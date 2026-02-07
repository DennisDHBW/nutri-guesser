package client.openfoodfacts.dto;

import lombok.Data;

@Data
public class SelectedImages {

    private SelectedImage front;

    private SelectedImage ingredients;

    private SelectedImage nutrition;
}
