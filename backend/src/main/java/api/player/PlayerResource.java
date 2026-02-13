package api.player;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import model.Player;
import repository.PlayerRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Path("/api/players")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PlayerResource {

    @Inject
    PlayerRepository playerRepository;

    @GET
    public List<Player> getAllPlayers() {
        return playerRepository.listAll();
    }

    @GET
    @Path("/{id}")
    public Response getPlayer(@PathParam("id") UUID id) {
        Player player = playerRepository.findById(id);
        if (player == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(player).build();
    }

    @POST
    @Transactional
    public Response createPlayer(PlayerCreateRequest request) {
        if (request.name == null || request.name.trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Name is required"))
                    .build();
        }

        if (request.name.length() > 12) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Name must not exceed 12 characters"))
                    .build();
        }

        Player player = new Player();
        player.playerId = UUID.randomUUID();
        player.nickname = request.name.trim();
        player.createdAt = LocalDateTime.now();

        playerRepository.persist(player);

        return Response.status(Response.Status.CREATED).entity(player).build();
    }

    public static class PlayerCreateRequest {
        public String name;
    }

    public static class ErrorResponse {
        public String message;

        public ErrorResponse(String message) {
            this.message = message;
        }
    }
}
