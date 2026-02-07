package api.openfoodfacts.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BarcodeResponse {
    private String barcode;
}