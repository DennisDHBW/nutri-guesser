package client.openfoodfacts.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class SearchResponse {
    private Integer count;
    private Integer page;
    private Integer page_size;
    private List<SearchProduct> products;

    @Getter
    public static class SearchProduct {
        private String code;
    }
}
