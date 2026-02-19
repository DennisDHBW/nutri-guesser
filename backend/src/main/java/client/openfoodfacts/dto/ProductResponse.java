package client.openfoodfacts.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record ProductResponse(
        Product product,
        String code,
        boolean status,
        @JsonProperty("status_verbose") String statusVerbose
) {}