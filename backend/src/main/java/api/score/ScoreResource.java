package api.score;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import client.score.dto.ScoreRequest;
import service.score.ScoreService;

@Path("/scores")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ScoreResource {

    @Inject
    ScoreService scoreService;

    @POST
    public double getCalculation(@Valid ScoreRequest request) {
        return scoreService.calculateScore(request);
    }
}
