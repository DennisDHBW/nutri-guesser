package api.openfoodfacts.dto;

public record RandomImageResponse(
        String barcode,
        String imageUrl
) {}