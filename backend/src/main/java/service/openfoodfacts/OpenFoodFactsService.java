package service.openfoodfacts;

import client.openfoodfacts.*;
import client.openfoodfacts.dto.ProductResponse;
import client.openfoodfacts.dto.SearchResponse;
import io.smallrye.faulttolerance.api.RateLimit;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import model.NutritionFacts;
import model.Product;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import repository.ProductRepository;

import java.time.temporal.ChronoUnit;
import java.util.List;

@ApplicationScoped
public class OpenFoodFactsService {

    @Inject
    ProductRepository productRepository;

    @RestClient
    OpenFoodFactsClient productClient;

    @RestClient
    OpenFoodFactsSearchClient searchClient;

    private static final int PRODUCTS_PER_MINUTE = 60;
    private static final int SEARCHES_PER_MINUTE = 10;
    private static final int MIN_DELAY_MS = 600;
    private static final int RATE_LIMIT_RETRY_DELAY = 5000;

    private long lastRequestTime = 0;

    @Transactional(TxType.REQUIRES_NEW)
    @RateLimit(value = PRODUCTS_PER_MINUTE, window = 1, windowUnit = ChronoUnit.MINUTES)
    public ProductResponse fetchProduct(String barcode) {
        int maxRetries = 3;
        int retryDelay = 1000;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                enforceRateLimit();

                Product cached = productRepository.findById(barcode);
                if (cached != null) {
                    return null;
                }

                long startTime = System.currentTimeMillis();
                ProductResponse remote;
                try {
                    remote = productClient.fetchProductByCode(barcode);
                } catch (Exception e) {
                    long duration = System.currentTimeMillis() - startTime;
                    System.err.printf("  [%d ms] API-Fehler beim Abrufen von %s: %s%n",
                            duration, barcode, e.getMessage());

                    if (e.getMessage() != null && e.getMessage().contains("rate limit")) {
                        if (attempt < maxRetries) {
                            System.out.printf("  Rate-Limit bei %s, Versuch %d/%d - warte %d ms%n",
                                    barcode, attempt, maxRetries, retryDelay * attempt);
                            Thread.sleep((long) retryDelay * attempt);
                            continue;
                        }
                    }
                    return null;
                }

                long apiDuration = System.currentTimeMillis() - startTime;
                System.out.printf("  [%d ms] API-Antwort für %s erhalten%n", apiDuration, barcode);

                if (remote != null && remote.product() != null && isValidProduct(remote)) {
                    long persistStart = System.currentTimeMillis();
                    try {
                        persistProduct(remote);
                        long persistDuration = System.currentTimeMillis() - persistStart;
                        System.out.printf("  [%d ms] Produkt %s gespeichert (gesamt: %d ms)%n",
                                persistDuration, barcode, System.currentTimeMillis() - startTime);
                        return remote;
                    } catch (Exception e) {
                        System.err.printf("  Fehler beim Speichern von %s: %s%n", barcode, e.getMessage());
                    }
                } else {
                    System.out.printf("  Produkt %s ungültig (kein Name, Barcode oder Kcal)%n", barcode);
                }
                return null;

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            } catch (Exception e) {
                if (attempt == maxRetries) {
                    System.err.printf("  Endgültiger Fehler bei %s nach %d Versuchen: %s%n",
                            barcode, maxRetries, e.getMessage());
                }
            }
        }
        return null;
    }

    private boolean isValidProduct(ProductResponse response) {
        if (response == null || response.product() == null) return false;

        String name = response.product().getProductName();
        if (name == null || name.trim().isEmpty()) {
            return false;
        }

        if (response.code() == null || response.code().trim().isEmpty()) {
            return false;
        }

        if (response.product().getNutriments() == null) {
            System.out.printf("  Produkt %s: Keine Nährwertangaben vorhanden%n", response.code());
            return false;
        }

        int kcal = response.product().getNutriments().getEnergyKcal100G();

        if (kcal < 0 || kcal > 1000) {
            System.out.printf("  Produkt %s: Ungültiger Kcal-Wert: %d%n", response.code(), kcal);
            return false;
        }

        return true;
    }

    @Transactional(TxType.MANDATORY)
    protected void persistProduct(ProductResponse productResponse) {
        if (productRepository.findById(productResponse.code()) != null) return;

        Product product = new Product();
        product.barcode = productResponse.code();
        product.name = productResponse.product().getProductName();
        product.brand = productResponse.product().getBrands();
        product.imageUrl = productResponse.product().getImageUrl();

        NutritionFacts nutritionFacts = new NutritionFacts();
        nutritionFacts.product = product;

        nutritionFacts.kcal100g = productResponse.product().getNutriments().getEnergyKcal100G();

        product.nutritionFacts = nutritionFacts;

        try {
            productRepository.persist(product);
        } catch (Exception e) {
            System.err.println("Datenbank-Fehler beim Speichern von " + productResponse.code() + ": " + e.getMessage());
            throw e;
        }
    }

    public long getLocalProductCount() {
        return productRepository.count();
    }

    @RateLimit(value = SEARCHES_PER_MINUTE, window = 1, windowUnit = ChronoUnit.MINUTES)
    public SearchResponse searchFood(String terms, int page) {
        try {
            long startTime = System.currentTimeMillis();
            SearchResponse response = searchClient.search(terms, 1, 1, 20, page);
            long duration = System.currentTimeMillis() - startTime;
            System.out.printf("  [%d ms] Suche nach '%s' Seite %d%n", duration, terms, page);
            return response;
        } catch (Exception e) {
            System.err.printf("  Fehler bei Suche '%s' Seite %d: %s%n", terms, page, e.getMessage());
            return null;
        }
    }

    private void enforceRateLimit() throws InterruptedException {
        long now = System.currentTimeMillis();
        if (lastRequestTime > 0) {
            long elapsed = now - lastRequestTime;
            if (elapsed < MIN_DELAY_MS) {
                long waitTime = MIN_DELAY_MS - elapsed;
                System.out.printf("  Rate-Limit Schutz: Warte %d ms...%n", waitTime);
                Thread.sleep(waitTime);
            }
        }
        lastRequestTime = System.currentTimeMillis();
    }

    public int loadAdditionalProducts(int additionalCount, String searchTerm) {
        long startCount = getLocalProductCount();
        long targetCount = startCount + additionalCount;

        System.out.printf("\n Lade %d neue Produkte mit Suchbegriff '%s'...%n", additionalCount, searchTerm);
        System.out.printf("Aktuell: %d Produkte in DB, Ziel: %d%n", startCount, targetCount);

        if (additionalCount <= 0) {
            System.out.println("Anzahl muss positiv sein");
            return 0;
        }

        int page = 1;
        int maxPages = 50;
        int consecutiveErrors = 0;
        int maxConsecutiveErrors = 5;
        int productsOnLastPage;
        int emptyPageCount = 0;

        while (getLocalProductCount() < targetCount && page <= maxPages && emptyPageCount < 3) {
            try {
                enforceRateLimit();

                SearchResponse searchResponse = searchFood(searchTerm, page);

                if (searchResponse == null || searchResponse.products() == null) {
                    System.out.printf("  Seite %d: Keine Antwort von der API%n", page);
                    consecutiveErrors++;
                    page++;
                    continue;
                }

                if (searchResponse.products().isEmpty()) {
                    emptyPageCount++;
                    System.out.printf("  Seite %d leer (leere Seiten: %d)%n", page, emptyPageCount);
                    page++;
                    continue;
                }

                emptyPageCount = 0;
                consecutiveErrors = 0;

                List<String> barcodes = searchResponse.products().stream()
                        .map(SearchResponse.SearchProduct::code)
                        .filter(code -> code != null && !code.trim().isEmpty())
                        .distinct()
                        .toList();

                productsOnLastPage = barcodes.size();
                System.out.printf(" Seite %d: %d Barcodes gefunden%n", page, productsOnLastPage);

                int successOnThisPage = 0;
                for (String barcode : barcodes) {
                    if (getLocalProductCount() >= targetCount) break;

                    try {
                        ProductResponse response = fetchProduct(barcode);

                        if (response != null) {
                            successOnThisPage++;
                            long current = getLocalProductCount();
                            long progress = current - startCount;
                            int percent = (int) ((progress * 100) / additionalCount);

                            System.out.printf("  Fortschritt: %d/%d (%d%%) - %d Produkte auf dieser Seite%n",
                                    progress, additionalCount, percent, successOnThisPage);
                        }

                    } catch (Exception e) {
                        System.err.printf("  Fehler bei Barcode %s: %s%n", barcode, e.getMessage());

                        if (e.getMessage() != null && e.getMessage().contains("rate limit")) {
                            System.out.printf("  Rate-Limit erreicht! Warte %d Sekunden...%n",
                                    RATE_LIMIT_RETRY_DELAY / 1000);
                            Thread.sleep(RATE_LIMIT_RETRY_DELAY);
                        }
                    }

                    Thread.sleep(50);
                }

                if (successOnThisPage == 0) {
                    System.out.printf("  Seite %d brachte keine neuen Produkte%n", page);
                }

                page++;

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Abbruch durch Interrupt");
                break;
            } catch (Exception e) {
                System.err.printf("Fehler bei Seite %d: %s%n", page, e.getMessage());
                consecutiveErrors++;
                page++;

                if (consecutiveErrors >= maxConsecutiveErrors) {
                    System.out.printf("Zu viele Fehler (%d). Breche ab.%n", consecutiveErrors);
                    break;
                }
            }
        }

        long finalCount = getLocalProductCount();
        long actuallyAdded = finalCount - startCount;

        System.out.println("\n" + "=".repeat(60));
        if (finalCount >= targetCount) {
            System.out.printf("ERFOLG: %d neue Produkte geladen! Gesamt jetzt: %d%n",
                    actuallyAdded, finalCount);
        } else {
            System.out.printf("TEILWEISER ERFOLG: %d von %d Produkten geladen. Gesamt jetzt: %d%n",
                    actuallyAdded, additionalCount, finalCount);

            if (page >= maxPages) {
                System.out.println("   Grund: Maximale Seitenanzahl erreicht");
            } else if (emptyPageCount >= 3) {
                System.out.println("   Grund: Keine weiteren Produkte verfügbar");
            } else if (consecutiveErrors >= maxConsecutiveErrors) {
                System.out.println("   Grund: Zu viele Fehler hintereinander");
            }
        }
        System.out.println("=".repeat(60));

        return (int) actuallyAdded;
    }
}