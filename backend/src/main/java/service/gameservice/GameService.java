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
        gameSessionRepository.persist(session);

        Product product = productRepository.findRandomFromDb();

        Round round = createRound(session.sessionId, product);

        return new StartGameResponse(session.sessionId, round.roundId, product.barcode, product.imageUrl);
    }

    public RoundResponse nextRound(UUID session) {
        if (session == null) {
            throw new IllegalArgumentException("Game session required");
        }

        if (gameSessionRepository.findById(session) == null) {
            throw new IllegalArgumentException("Game session not found: " + session);
        }

        Product product = productRepository.findRandomFromDb();
        Round round = createRound(session, product);

        return new RoundResponse(round.roundId, product.barcode, product.imageUrl);
    }

    @Transactional
    private Round createRound(UUID sessionId, Product product){
        GameSession gameSession = gameSessionRepository.findById(sessionId);
        Round round = new Round();
        round.session = gameSession;
        round.product = product;
        round.roundNumber = Math.toIntExact(roundRepository.count("session", sessionId) + 1);
        roundRepository.persist(round);
        return round;
    }

}


