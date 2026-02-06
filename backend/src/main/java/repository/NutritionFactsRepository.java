package repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import model.NutritionFacts;

@ApplicationScoped
public class NutritionFactsRepository implements PanacheRepositoryBase<NutritionFacts, String> {
    // Hier nutzen wir meist nur die Standard-Methoden wie findById(barcode)
}