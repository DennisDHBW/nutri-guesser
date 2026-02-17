package api.result.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record ResultResponseDTO(
        String id,
        List<String> tags,
        @JsonProperty("created_at") String createdAt,
        String url,
        String mimetype,
        Integer rank,
        Integer totalScore,
        Float betterThanPercentage
) {}
