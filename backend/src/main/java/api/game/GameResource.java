package api.game;

import client.gameresource.dto.RoundResponse;
import client.gameresource.dto.StartGameRequest;
import client.gameresource.dto.StartGameResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import service.gameservice.GameService;
import java.util.UUID;

@Path("/api/game")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class GameResource {

    @Inject
    GameService gameService;

    @POST
    @Path("/start")
    @Transactional
    public StartGameResponse startGameSession(StartGameRequest request) {
        return gameService.startGameSession(request);
    }

    @GET
    @Path("/nextround")
    public RoundResponse getNextProduct(@QueryParam("sessionId") UUID sessionId) {
        return gameService.nextRound(sessionId);
    }
}





