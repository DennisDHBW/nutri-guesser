package client.score.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ScoreResponse(
        @JsonProperty("points")
        int points,

        @JsonProperty("actualKcal")
        int actualKcal,

        @JsonProperty("isLastRound")
        boolean isLastRound
) {
}
