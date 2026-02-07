package client.openfoodfacts.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductResponse {

    private Product product;

    private String code;

    private boolean status;

    @JsonProperty("status_verbose")
    private String statusVerbose;
}
