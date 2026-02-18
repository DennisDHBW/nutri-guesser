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

    @Transactional(TxType.REQUIRES_NEW)
    @RateLimit(value = 100, window = 1, windowUnit = ChronoUnit.MINUTES)
    public ProductResponse fetchProduct(String barcode) {

        Product cached = productRepository.findById(barcode);
        if (cached != null) {
            return null;
        }

        ProductResponse remote = null;
        try {
            remote = productClient.fetchProductByCode(barcode);
        } catch (Exception e) {
            System.err.println("API-Fehler beim Abrufen von " + barcode + ": " + e.getMessage());
            return null;
        }

        if (remote != null && remote.getProduct() != null && isValidProduct(remote)) {
            try {
                persistProduct(remote);
                return remote;
            } catch (Exception e) {
                System.err.println("Fehler beim Speichern von " + barcode + ": " + e.getMessage());
            }
        }
        return null;
    }

    private boolean isValidProduct(ProductResponse response) {
        if (response == null || response.getProduct() == null) return false;

        String name = response.getProduct().getProductName();
        if (name == null || name.trim().isEmpty()) {
            return false;
        }

        return response.getCode() != null && !response.getCode().trim().isEmpty();
    }

    @Transactional(TxType.MANDATORY)
    protected void persistProduct(ProductResponse productResponse) {
        if (productRepository.findById(productResponse.getCode()) != null) return;

        Product product = new Product();
        product.barcode = productResponse.getCode();
        product.name = productResponse.getProduct().getProductName();
        product.brand = productResponse.getProduct().getBrands();
        product.imageUrl = productResponse.getProduct().getImageUrl();

        NutritionFacts nutritionFacts = new NutritionFacts();
        nutritionFacts.product = product;

        if (productResponse.getProduct().getNutriments() != null) {
            nutritionFacts.kcal100g = productResponse.getProduct().getNutriments().getEnergyKcal100G();
        }

        product.nutritionFacts = nutritionFacts;

        try {
            productRepository.persist(product);
        } catch (Exception e) {
            System.err.println("Datenbank-Fehler beim Speichern von " + productResponse.getCode() + ": " + e.getMessage());
            throw e;
        }
    }

    public long getLocalProductCount() {
        return productRepository.count();
    }

    public Product getRandomLocal() {
        return productRepository.findRandomFromDb();
    }

    @RateLimit(value = 10, window = 1, windowUnit = ChronoUnit.MINUTES)
    public SearchResponse searchFood(String terms, int page) {
        try {
            return searchClient.search(terms, 1, 1, 20, page);
        } catch (Exception e) {
            System.err.println("Fehler bei der Suche nach '" + terms + "' Seite " + page + ": " + e.getMessage());
            return null;
        }
    }


    public int loadAdditionalProducts(int additionalCount, String searchTerm) {
        long startCount = getLocalProductCount();
        long targetCount = startCount + additionalCount;

        System.out.println("Lade " + additionalCount + " neue Produkte... Aktuell: " + startCount + ", Ziel: " + targetCount);

        if (additionalCount <= 0) {
            System.out.println("Anzahl muss positiv sein");
            return 0;
        }

        int page = 1;
        int loadedProducts = 0;
        int maxPages = 50;

        while (getLocalProductCount() < targetCount && page <= maxPages) {
            try {
                SearchResponse searchResponse = searchFood(searchTerm, page);

                if (searchResponse == null || searchResponse.getProducts() == null || searchResponse.getProducts().isEmpty()) {
                    System.out.println("Keine weiteren Produkte auf Seite " + page);
                    break;
                }

                List<String> barcodes = searchResponse.getProducts().stream()
                        .map(SearchResponse.SearchProduct::getCode)
                        .filter(code -> code != null && !code.trim().isEmpty())
                        .distinct()
                        .toList();

                System.out.println("Seite " + page + ": " + barcodes.size() + " Barcodes gefunden");

                for (String barcode : barcodes) {
                    if (getLocalProductCount() >= targetCount) break;

                    try {
                        ProductResponse response = fetchProduct(barcode);
                        if (response != null) {
                            loadedProducts++;
                            if (loadedProducts % 5 == 0) {
                                long current = getLocalProductCount();
                                System.out.println("Fortschritt: " + (current - startCount) + "/" + additionalCount + " neue Produkte geladen");
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("Fehler bei Barcode " + barcode + ": " + e.getMessage());
                    }

                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }

                page++;

            } catch (Exception e) {
                System.err.println("Fehler bei Seite " + page + ": " + e.getMessage());
                page++;
            }
        }

        long finalCount = getLocalProductCount();
        long actuallyAdded = finalCount - startCount;
        System.out.println("Fertig! " + actuallyAdded + " von " + additionalCount + " gewünschten Produkten geladen. Gesamt jetzt: " + finalCount);

        return (int) actuallyAdded;
    }
}