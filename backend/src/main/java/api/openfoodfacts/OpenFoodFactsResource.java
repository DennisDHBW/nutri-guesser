package api.openfoodfacts;

import api.openfoodfacts.dto.RandomImageResponse;
import api.openfoodfacts.dto.PopulationResponse;
import jakarta.ws.rs.*;
import service.openfoodfacts.OpenFoodFactsService;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;

@Path("/api/off")
@Produces(MediaType.APPLICATION_JSON)
public class OpenFoodFactsResource {

    @Inject
    OpenFoodFactsService offService;

    @GET
    @Path("/random")
    public RandomImageResponse getRandom() {
        model.Product product = offService.getRandomLocal();

        if (product != null && product.imageUrl != null) {
            return new RandomImageResponse(product.barcode, product.imageUrl);
        }

        return new RandomImageResponse(null, "Keine Produkte in der Datenbank. Bitte zuerst /api/off/admin/load/50 aufrufen.");
    }

    @POST
    @Path("/admin/load/{count}")
    @Produces(MediaType.APPLICATION_JSON)
    public PopulationResponse loadAdditionalProducts(@PathParam("count") int count) {
        try {
            long before = offService.getLocalProductCount();
            int loaded = offService.loadAdditionalProducts(count, "food");
            long after = offService.getLocalProductCount();

            return new PopulationResponse(
                    before,
                    after,
                    loaded,
                    after,
                    String.format("%d neue Produkte geladen. Gesamt jetzt: %d", loaded, after)
            );
        } catch (Exception e) {
            e.printStackTrace();
            long current = offService.getLocalProductCount();
            return new PopulationResponse(
                    current,
                    current,
                    0,
                    current,
                    "Fehler: " + e.getMessage()
            );
        }
    }

    @POST
    @Path("/admin/load/{searchTerm}/{count}")
    @Produces(MediaType.APPLICATION_JSON)
    public PopulationResponse loadAdditionalProductsWithSearch(
            @PathParam("searchTerm") String searchTerm,
            @PathParam("count") int count) {
        try {
            long before = offService.getLocalProductCount();
            int loaded = offService.loadAdditionalProducts(count, searchTerm);
            long after = offService.getLocalProductCount();

            return new PopulationResponse(
                    before,
                    after,
                    loaded,
                    after,
                    String.format("%d neue Produkte mit Suchbegriff '%s' geladen. Gesamt jetzt: %d",
                            loaded, searchTerm, after)
            );
        } catch (Exception e) {
            e.printStackTrace();
            long current = offService.getLocalProductCount();
            return new PopulationResponse(
                    current,
                    current,
                    0,
                    current,
                    "Fehler: " + e.getMessage()
            );
        }
    }

    @GET
    @Path("/admin/count")
    @Produces(MediaType.TEXT_PLAIN)
    public long getCount() {
        return offService.getLocalProductCount();
    }
}