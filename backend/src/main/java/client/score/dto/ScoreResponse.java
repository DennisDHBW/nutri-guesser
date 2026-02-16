package client.score.dto;

public record ScoreResponse(
        int points,
        int actualKcal
) {
}
