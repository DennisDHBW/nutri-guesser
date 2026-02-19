package client.openfoodfacts.dto;

public record SelectedImages(
        SelectedImage front,
        SelectedImage ingredients,
        SelectedImage nutrition
) {}