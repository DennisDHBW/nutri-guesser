

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import model.NutritionFacts;
import service.GameService;

import java.util.Optional;

@ApplicationScoped
public class TestStartup {

    @Inject
    GameService gameService;

    // Diese Methode wird beim Start der App aufgerufen
    void onStart(@Observes StartupEvent ev) {
        System.out.println("========================================");
        System.out.println("🚀 STARTE DATENBANK-TEST (MAGIC NUMBERS)");
        System.out.println("========================================");

        String testBarcode = "4001234567890"; // Unsere Magic Number

        try {
            // 1. Einfügen (Insert)
            gameService.addProductWithNutrition(
                    testBarcode,
                    "Super Pizza",
                    "Pizza Brand",
                    255.50F, 12.40, 32.10, 9.50
            );
            System.out.println("✅ Schritt 1: Produkt & Nährwerte gespeichert.");

            // 2. Auslesen (Select)
            Optional<NutritionFacts> facts = gameService.getNutritionFacts(testBarcode);

            if (facts.isPresent()) {
                NutritionFacts f = facts.get();
                System.out.println("✅ Schritt 2: Daten erfolgreich gelesen.");
                System.out.println("   -> Name: " + f.product.name);
                System.out.println("   -> Kcal: " + f.kcal100g);
            } else {
                System.out.println("❌ Schritt 2: Daten wurden nicht gefunden!");
            }

        } catch (Exception e) {
            System.err.println("💥 TEST FEHLGESCHLAGEN: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("========================================");
    }
}
