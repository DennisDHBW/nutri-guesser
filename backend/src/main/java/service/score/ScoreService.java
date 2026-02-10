package service.score;

import jakarta.enterprise.context.ApplicationScoped;
import client.score.dto.ScoreRequest;

@ApplicationScoped
public class ScoreService {

    public double calculateScore(ScoreRequest request) {

        return (request.guessed_Min() + request.guessed_Max()) / 3.0;
    }
}
