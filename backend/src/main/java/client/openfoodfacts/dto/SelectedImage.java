package client.openfoodfacts.dto;

public record SelectedImage(
        SelectedImageItem display,
        SelectedImageItem small,
        SelectedImageItem thumb
) {}
