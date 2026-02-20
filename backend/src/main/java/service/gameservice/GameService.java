package service.gameservice;

import client.gameresource.dto.RoundResponse;
import client.gameresource.dto.StartGameRequest;
import client.gameresource.dto.StartGameResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import model.*;
import repository.*;

import java.time.LocalDateTime;
import java.util.UUID;

@ApplicationScoped
public class GameService {
    public static final int MAX_ROUNDS = 5;

    @Inject
    ProductRepository productRepository;

    @Inject
    PlayerRepository playerRepository;

    @Inject
    GameSessionRepository gameSessionRepository;

    @Inject
    RoundRepository roundRepository;

    @Transactional
    public StartGameResponse startGameSession(StartGameRequest request) {
        if(request.nickname() == null || request.nickname().trim().isEmpty()) {
            throw new IllegalArgumentException("Nickname cannot be null or empty");
        }

        // Create new player
        Player player = new Player();
        player.nickname = request.nickname();
        playerRepository.persist(player);

        // Create new game session
        GameSession session = new GameSession();
        session.player = player;
        session.startedAt = LocalDateTime.now();
        session.totalScore = 0;
        gameSessionRepository.persist(session);

        Product product = productRepository.findRandomFromDb();
        if (product == null) {
            throw new IllegalStateException("No products available. Ensure test data is loaded.");
        }

        Round round = createRound(session, product, 1);

        return new StartGameResponse(session.sessionId, round.roundId, product.barcode, product.imageUrl, product.name);
    }

    @Transactional
    public RoundResponse nextRound(UUID sessionId) {
        GameSession session = gameSessionRepository.findById(sessionId);
        if (session == null) {
            throw new IllegalArgumentException("Game session not found: " + sessionId);
        }

        long roundCount = roundRepository.count("session.sessionId", sessionId);
        if (roundCount >= MAX_ROUNDS) {
            throw new IllegalStateException("Maximum rounds reached for this session");
        }

        long totalProducts = productRepository.count();
        if (totalProducts == 0) {
            throw new IllegalStateException("No products available.");
        }

        Product product = getUniqueProduct(sessionId, totalProducts);

        Round round = createRound(session, product, (int) (roundCount + 1));

        return new RoundResponse(round.roundId, product.barcode, product.imageUrl, product.name);
    }

    private Product getUniqueProduct(UUID sessionId, long totalProducts) {
        Product product = productRepository.findRandomFromDb();
        if (totalProducts > MAX_ROUNDS) {
            long isDuplicate = roundRepository.count(
                    "session.sessionId = ?1 AND product.barcode = ?2", sessionId, product.barcode
            );

            if (isDuplicate > 0) {
                return getUniqueProduct(sessionId, totalProducts);
            }
        }
        return product;
    }

    private Round createRound(GameSession session, Product product, int roundNumber){
        Round round = new Round();
        round.session = session;
        round.product = product;
        round.roundNumber = roundNumber;
        roundRepository.persist(round);
        return round;
    }

}
