package client.openfoodfacts.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Source(
        String[] fields,
        String id,
        String[] images,
        @JsonProperty("import_t") long importT,
        String manufacturer,
        String name,
        String url
) {}