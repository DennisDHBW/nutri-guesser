package api.score;

import client.score.dto.ScoreResponse;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import client.score.dto.ScoreRequest;
import service.score.ScoreService;

@Path("/score")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ScoreResource {

    @Inject
    ScoreService scoreService;

    @POST
    public ScoreResponse getCalculation(@Valid ScoreRequest request) {
        return scoreService.calculateScore(request);
    }
}
