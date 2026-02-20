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

        Round round = createRound(session, product);

        return new StartGameResponse(session.sessionId, round.roundId, product.barcode, product.imageUrl, product.name);
    }

    @Transactional
    public RoundResponse nextRound(UUID sessionId) {
        GameSession session = gameSessionRepository.findById(sessionId);
        if (session == null) {
            throw new IllegalArgumentException("Game session not found: " + sessionId);
        }

        if (roundRepository.count("session.sessionId", sessionId) >= 5) {
            throw new IllegalStateException("Maximum rounds reached for this session");
        }

        Product product = productRepository.findRandomFromDb();
        if (product == null) {
            throw new IllegalStateException("No products available. Ensure test data is loaded.");
        }
        Round round = createRound(session, product);

        return new RoundResponse(round.roundId, product.barcode, product.imageUrl, product.name);
    }


    private Round createRound(GameSession session, Product product){
        Round round = new Round();
        round.session = session;
        round.product = product;
        long count = roundRepository.count("session.sessionId", session.sessionId);
        round.roundNumber = (int) count + 1;
        roundRepository.persist(round);
        return round;
    }

}
