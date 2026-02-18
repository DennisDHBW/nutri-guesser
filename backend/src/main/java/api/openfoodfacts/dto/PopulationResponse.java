package api.openfoodfacts.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PopulationResponse {
    public long productsBefore;
    public long productsAfter;
    public int productsLoaded;
    public long productsTotal;
    public String message;
}