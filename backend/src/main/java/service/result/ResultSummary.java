package service.result;

import java.util.List;

public record ResultSummary(
        String id,
        List<String> tags,
        String createdAt,
        String url,
        String mimetype,
        Integer rank,
        Integer totalScore,
        Float betterThanPercentage
) {}

