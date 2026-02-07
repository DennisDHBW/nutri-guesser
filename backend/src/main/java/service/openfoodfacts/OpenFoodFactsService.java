package service.openfoodfacts;

import client.openfoodfacts.*;
import client.openfoodfacts.dto.ProductResponse;
import client.openfoodfacts.dto.SearchResponse;
import io.smallrye.faulttolerance.api.RateLimit;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import model.NutritionFacts;
import model.Product;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import repository.ProductRepository;

import java.time.temporal.ChronoUnit;

@ApplicationScoped
public class OpenFoodFactsService {

    @Inject
    ProductRepository productRepository;

    @RestClient
    OpenFoodFactsClient productClient;

    @RestClient
    OpenFoodFactsSearchClient searchClient;

    @Transactional
    @RateLimit(value = 100, window = 1, windowUnit = ChronoUnit.MINUTES)
    public ProductResponse fetchProduct(String barcode) {
        // 1. Check H2 Cache
        Product cached = productRepository.findById(barcode);
        if (cached != null) {
            return null; //mapToResponse(cached);
        }

        // 2. API Call if not in Cache
        ProductResponse remote = productClient.fetchProductByCode(barcode);

        // 3. Persist for future use (Auto-Cache)
        if (remote != null && remote.getProduct() != null) {
            persistProduct(remote);
        }
        return remote;
    }

    @Transactional
    protected void persistProduct(ProductResponse productResponse) {
        if (productRepository.findById(productResponse.getCode()) != null) return;

        Product product = new Product();
        product.barcode = productResponse.getCode();
        product.name = productResponse.getProduct().getProductName();
        product.brand = productResponse.getProduct().getBrands();

        NutritionFacts nutritionFacts;
        nutritionFacts = new NutritionFacts();
        nutritionFacts.product = product;
        nutritionFacts.kcal100g = productResponse.getProduct().getNutriments().getEnergyKcal100G();

        product.nutritionFacts = nutritionFacts;

        productRepository.persist(product);
    }

    /*
    private ProductResponse mapToResponse(Product product) {
        // Hilfsmethode um Entity zurück in DTO zu wandeln, falls nötig
        return new ProductResponse(product.barcode, null); // Vereinfacht
    }
    */

    public long getLocalProductCount() {
        return productRepository.count();
    }

    public Product getRandomLocal() {
        return productRepository.findRandomFromDb();
    }

    @RateLimit(value = 10, window = 1, windowUnit = ChronoUnit.MINUTES)
    public SearchResponse searchFood(String terms, int page) {
        return searchClient.search(terms, 1, 1, 20, page);
    }
}
