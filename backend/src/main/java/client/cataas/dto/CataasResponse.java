package client.cataas.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record CataasResponse(
        @JsonProperty("_id") String id,
        List<String> tags,
        @JsonProperty("created_at") String createdAt,
        String url,
        String mimetype
) {}