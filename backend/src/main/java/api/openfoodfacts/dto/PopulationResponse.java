package api.openfoodfacts.dto;

public record PopulationResponse(
        long productsBefore,
        long productsAfter,
        int productsLoaded,
        long productsTotal,
        String message
) {}