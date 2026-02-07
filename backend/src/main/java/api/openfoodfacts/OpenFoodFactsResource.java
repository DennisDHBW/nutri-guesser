package api.openfoodfacts;

import api.openfoodfacts.dto.ImageResponse;
import api.openfoodfacts.dto.RandomImageResponse;
import client.openfoodfacts.dto.Product;
import client.openfoodfacts.dto.ProductResponse;
import client.openfoodfacts.dto.SearchResponse;
import jakarta.ws.rs.*;
import service.openfoodfacts.OpenFoodFactsService;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import java.util.Objects;
import java.util.Random;

@Path("/api/off")
@Produces(MediaType.APPLICATION_JSON)
public class OpenFoodFactsResource {

    @Inject
    OpenFoodFactsService offService;

    private final Random random = new Random();

    // dieser Endpoint dient zum Testen der Anbindung an OpenFoodFacts und gibt das komplette Produkt-JSON zurück
    @GET
    @Path("/product/{barcode}")
    public ProductResponse fetchProductByCode(@PathParam("barcode") String barcode) {
        return offService.fetchProduct(barcode);
    }

    // dieser Endpoint gibt nur die Bild-URL des Produkts zurück (auch nur zum testen)
    @GET
    @Path("/image/{barcode}")
    public ImageResponse image(@PathParam("barcode") String barcode) {
        ProductResponse response = offService.fetchProduct(barcode);
        String imageUrl = extractImageUrl(response);
        return new ImageResponse(imageUrl);
    }

    @GET
    @Path("/random")
    public RandomImageResponse getRandom() {

        long count = offService.getLocalProductCount();

        // Strategie: Wenn DB voll genug, 70% Chance auf DB, sonst API
        if (count > 20 && random.nextInt(100) < 70) {
            model.Product product = offService.getRandomLocal();
            if (product != null && product.imageUrl != null) {
                return new RandomImageResponse(product.barcode, product.imageUrl);
            }
        }

        // Deep Search in API (Seite 1 bis 500), um Wiederholung zu vermeiden
        int randomPage = 1 + random.nextInt(500);
        SearchResponse searchResponse = offService.searchFood("food", randomPage);
        String barcode = pickRandomBarcode(searchResponse);

        ProductResponse product = offService.fetchProduct(barcode);
        String imageUrl = extractImageUrl(product);

        if (imageUrl != null) {
            return new RandomImageResponse(barcode, imageUrl);
        }
        return new RandomImageResponse(null, null);
    }

    private String pickRandomBarcode(SearchResponse page) {
        if (page == null || page.getProducts() == null || page.getProducts().isEmpty()) return null;

        List<String> codes = page.getProducts().stream()
                .map(SearchResponse.SearchProduct::getCode)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        if (codes.isEmpty()) return null;

        return codes.get(random.nextInt(codes.size()));
    }

    private String extractImageUrl(ProductResponse response) {
        if (response == null || response.getProduct() == null) return null;

        Product product = response.getProduct();
        if (product.getImageUrl() != null && !product.getImageUrl().isBlank()) {
            return product.getImageUrl();
        }
        return null;
    }
}
