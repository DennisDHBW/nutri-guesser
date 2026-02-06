package repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import model.LeaderboardEntry;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class LeaderboardRepository implements PanacheRepositoryBase<LeaderboardEntry, UUID> {

    // Die eigentliche "Highscore"-Abfrage
    public List<LeaderboardEntry> findTopScores(int limit) {
        return find("order by score desc").page(0, limit).list();
    }
}
