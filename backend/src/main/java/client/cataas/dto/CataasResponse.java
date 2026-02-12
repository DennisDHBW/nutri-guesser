package client.cataas.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record CataasResponse(
        String id,
        List<String> tags,
        @JsonProperty("created_at") String createdAt,
        String url,
        String mimetype
) {}