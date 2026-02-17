package repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import model.LeaderboardEntry;
import model.GameSession;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class LeaderboardRepository implements PanacheRepositoryBase<LeaderboardEntry, UUID> {

    public List<LeaderboardEntry> findTopEntries(int limit) {
        return find("order by score desc").page(0, limit).list();
    }

    public Integer getPredictedPlacement(Long score) {
        Long count = count("score > ?1", score);
        return count.intValue() + 1;
    }

    public Float calculatePercentile(Long score) {
        long totalScores = count();
        if (totalScores <= 1) {
            return 100.0f;
        }

        long betterScores = count("score > ?1", score);
        float percentile = (totalScores - betterScores - 1) * 100.0f / (totalScores - 1);

        return Math.max(0, Math.min(100, Math.round(percentile * 100) / 100.0f));
    }

    public LeaderboardEntry findBySession(GameSession session) {
        return find("session = ?1", session).firstResult();
    }

}
