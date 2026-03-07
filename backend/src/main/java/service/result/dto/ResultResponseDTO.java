package service.result.dto;

import client.cataas.dto.CataasResponse;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

public record ResultResponseDTO(
        @JsonUnwrapped CataasResponse catImage,
        Integer rank,
        Integer totalScore,
        Float betterThanPercentage
) {}

