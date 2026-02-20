package client.score.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.UUID;

public record ScoreRequest(
        @NotNull @PositiveOrZero Integer guessedMin,
        @NotNull @PositiveOrZero Integer guessedMax,
        @NotNull UUID roundId,
        @NotNull String barcode
) {
    @AssertTrue(message = "Unplausible values")
    public boolean isRangeValid() {
        if (guessedMin == null || guessedMax == null) {
            return true; // @NotNull will find the error
        }
        return guessedMin <= guessedMax;
    }
}
