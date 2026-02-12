package repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import model.GameSession;
import model.Player;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class GameSessionRepository implements PanacheRepositoryBase<GameSession, UUID> {
    public List<GameSession> listByPlayer(Player player) {
        return list("player", "startedAt", player);
    }
}
