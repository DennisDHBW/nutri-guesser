package api.game;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import model.GameSession;
import model.Player;
import model.Product;
import model.Round;
import repository.GameSessionRepository;
import repository.PlayerRepository;
import repository.ProductRepository;
import repository.RoundRepository;
import service.GameService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Path("/api/game")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class GameResource {

    @Inject
    GameService gameService;

    @Inject
    GameSessionRepository gameSessionRepository;

    @Inject
    PlayerRepository playerRepository;

    @Inject
    ProductRepository productRepository;

    @Inject
    RoundRepository roundRepository;

    @POST
    @Path("/start")
    @Transactional
    public Response startGameSession(@QueryParam("playerId") UUID playerId) {
        if (playerId == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("playerId is required"))
                    .build();
        }

        Player player = playerRepository.findById(playerId);
        if (player == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse("Player not found"))
                    .build();
        }

        GameSession session = new GameSession();
        session.sessionId = UUID.randomUUID();
        session.player = player;
        session.startedAt = LocalDateTime.now();
        session.totalScore = 0;

        gameSessionRepository.persist(session);

        return Response.status(Response.Status.CREATED).entity(session).build();
    }

    @GET
    @Path("/next-product")
    public Response getNextProduct(@QueryParam("sessionId") UUID sessionId) {
        if (sessionId == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("sessionId is required"))
                    .build();
        }

        GameSession session = gameSessionRepository.findById(sessionId);
        if (session == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse("Game session not found"))
                    .build();
        }

        try {
            // Hole zufälliges Produkt
            Product product = gameService.getRandomProduct();

            if (product == null) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity(new ErrorResponse("No products available"))
                        .build();
            }

            ProductResponse response = new ProductResponse();
            response.id = product.barcode;
            response.productName = product.name;
            response.brand = product.brand;
            response.imageUrl = product.imageUrl;

            if (product.nutritionFacts != null) {
                response.calories = (int) product.nutritionFacts.kcal100g;
            }

            return Response.ok(response).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Error fetching product: " + e.getMessage()))
                    .build();
        }
    }

    @POST
    @Path("/guess")
    @Transactional
    public Response submitGuess(GuessRequest request) {
        if (request.sessionId == null || request.productId == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("sessionId and productId are required"))
                    .build();
        }

        GameSession session = gameSessionRepository.findById(request.sessionId);
        if (session == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse("Game session not found"))
                    .build();
        }

        Product product = productRepository.findById(request.productId);
        if (product == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse("Product not found"))
                    .build();
        }

        // Berechne Punkte
        int actualCalories = product.nutritionFacts != null ?
                (int) product.nutritionFacts.kcal100g : 0;

        boolean correct = actualCalories >= request.minCalories &&
                         actualCalories <= request.maxCalories;

        int points = 0;
        if (correct) {
            // Je enger die Spanne, desto mehr Punkte
            int range = request.maxCalories - request.minCalories;
            if (range <= 50) points = 100;
            else if (range <= 100) points = 90;
            else if (range <= 200) points = 80;
            else if (range <= 300) points = 70;
            else if (range <= 400) points = 60;
            else if (range <= 500) points = 50;
            else points = 40;
        }

        // Erstelle Round
        Round round = new Round();
        round.roundId = UUID.randomUUID();
        round.session = session;
        round.product = product;
        round.guessedRange = request.minCalories + "-" + request.maxCalories;
        round.actualKcal = actualCalories;
        round.points = points;
        round.roundNumber = (int) roundRepository.count("session = ?1", session) + 1;

        roundRepository.persist(round);

        // Update Session - zähle Runden
        long roundCount = roundRepository.count("session = ?1", session);
        if (roundCount >= 5) {
            session.endedAt = LocalDateTime.now();
            // Berechne Gesamtpunktzahl
            List<Round> allRounds = roundRepository.list("session", session);
            int totalScore = allRounds.stream().mapToInt(r -> r.points).sum();
            session.totalScore = totalScore;
        }
        gameSessionRepository.persist(session);

        GuessResponse response = new GuessResponse();
        response.points = points;
        response.correct = correct;
        response.actualCalories = actualCalories;
        response.message = correct ? "Richtig!" : "Leider falsch!";

        return Response.ok(response).build();
    }

    public static class ErrorResponse {
        public String message;

        public ErrorResponse(String message) {
            this.message = message;
        }
    }

    public static class ProductResponse {
        public String id;
        public String productName;
        public String brand;
        public String imageUrl;
        public Integer calories;
    }

    public static class GuessRequest {
        public UUID sessionId;
        public String productId;
        public int minCalories;
        public int maxCalories;
    }

    public static class GuessResponse {
        public int points;
        public boolean correct;
        public int actualCalories;
        public String message;
    }
}





