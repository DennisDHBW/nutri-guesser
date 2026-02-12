package service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import model.NutritionFacts;
import model.Product;
import repository.NutritionFactsRepository;
import repository.ProductRepository;

import java.math.BigDecimal;
import java.util.Optional;

@ApplicationScoped
public class GameService {

    @Inject
    ProductRepository productRepository;

    @Inject
    NutritionFactsRepository nutritionFactsRepository;

    @Transactional
    public void addProductWithNutrition(String barcode, String name, String brand,
                                        float kcal, double fat, double carbs, double protein) {

        Product product = new Product();
        product.barcode = barcode;
        product.name = name;
        product.brand = brand;
        productRepository.persist(product);

        NutritionFacts facts = new NutritionFacts();
        facts.product = product;
        facts.kcal100g = kcal;

        nutritionFactsRepository.persist(facts);
    }

    public Optional<NutritionFacts> getNutritionFacts(String barcode) {
        return nutritionFactsRepository.findByIdOptional(barcode);
    }
}
