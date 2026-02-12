package repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import model.Round;
import model.GameSession;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class RoundRepository implements PanacheRepositoryBase<Round, UUID> {

    public List<Round> listBySession(GameSession session) {
        return list("session", session);
    }
}
