package client.openfoodfacts.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Ingredient(
        @JsonProperty("from_palm_oil") String fromPalmOil,
        String id,
        String origin,
        String percent,
        int rank,
        String text,
        String vegan,
        String vegetarian
) {}