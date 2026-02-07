package api.openfoodfacts.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RandomImageResponse {
    private String barcode;
    private String imageUrl;
}
