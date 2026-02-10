package client.score.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.UUID;

public record ScoreRequest(
        @NotNull @PositiveOrZero Double guessed_Min,
        @NotNull @PositiveOrZero Double guessed_Max,
        @NotNull UUID roundId,
        @NotNull String barcode
) {
    @AssertTrue(message = "Unplausible values")
    public boolean isRangeValid() {
        if (guessed_Min == null || guessed_Max == null) {
            return true; // @NotNull will find the error
        }
        return guessed_Min < guessed_Max;
    }
}
