package client.openfoodfacts.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record NutrientLevels(
        String fat,
        String salt,
        @JsonProperty("saturated-fat") String saturatedFat,
        String sugars
) {}