package client.openfoodfacts.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record SearchResponse(
        Integer count,
        Integer page,
        @JsonProperty("page_size") Integer pageSize,
        List<SearchProduct> products
) {
    public record SearchProduct(String code) {}
}