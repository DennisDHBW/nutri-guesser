package service.score;

import client.score.dto.ScoreResponse;
import jakarta.enterprise.context.ApplicationScoped;
import client.score.dto.ScoreRequest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import model.Round;
import repository.NutritionFactsRepository;
import repository.RoundRepository;

import java.util.UUID;

import static java.lang.Math.exp;

@ApplicationScoped
public class ScoreService {

    private static final double MAX_GUESS_RANGE = 1000.0;
    private static final double SCORE_EXP_FACTOR = 0.005;
    private static final double SCORE_MULTIPLIER = 0.675;

    @Inject
    RoundRepository roundRepository;

    @Inject
    NutritionFactsRepository nutritionFactsRepository;

    @Transactional
    public ScoreResponse calculateScore(ScoreRequest request) {
        Round round = roundRepository.findById(request.roundId());
        if (round == null) {
            throw new IllegalArgumentException("Round not found: " + request.roundId());
        }
        if (round.points != null && round.points != 0) {
            throw new IllegalStateException("This round has already been scored.");
        }

        round.guessedMin = request.guessedMin();
        round.guessedMax = request.guessedMax();

        var nutritionFacts = nutritionFactsRepository.findById(request.barcode());
        if (nutritionFacts == null) {
            throw new IllegalArgumentException("Product data not found for barcode: " + request.barcode());
        }

        float kcal = nutritionFacts.kcal100g;
        round.actualKcal = (int) kcal;

        int guessedRange = request.guessedMax() - request.guessedMin();
        int finalScore = 0;

        if (request.guessedMin() <= kcal && kcal <= request.guessedMax()) {
            double rawScore = SCORE_MULTIPLIER * exp(SCORE_EXP_FACTOR * (MAX_GUESS_RANGE - guessedRange) - 1);
            finalScore = (int) Math.round(rawScore);
        }

        round.points = finalScore;

        boolean isLastRound = numRounds(round.session.sessionId);

        // Due to @Transactional round is persisted automatically.
        return new ScoreResponse(round.points, round.actualKcal, isLastRound);
    }

    private boolean numRounds(UUID sessionId) {
        return roundRepository.count("session.sessionId", sessionId) >= 5;
    }
}
