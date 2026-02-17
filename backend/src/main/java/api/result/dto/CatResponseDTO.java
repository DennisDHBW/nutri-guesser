package api.result.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record CatResponseDTO(
        String id,
        List<String> tags,
        @JsonProperty("created_at") String createdAt,
        String url,
        String mimetype
) {}

