package model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "LEADERBOARD_ENTRY")
public class LeaderboardEntry extends PanacheEntityBase {
    @Id
    @Column(name = "ENTRY_ID")
    public UUID entryId; //

    @ManyToOne
    @JoinColumn(name = "PLAYER_ID", nullable = false)
    public Player player; // [cite: 100]

    @ManyToOne
    @JoinColumn(name = "SESSION_ID", nullable = false)
    public GameSession session; // [cite: 101]

    @Column(name = "RANK")
    public Integer rank; //

    @Column(name = "SCORE", nullable = false)
    public Integer score; //

    @Column(name = "ACHIEVED_AT")
    public LocalDateTime achievedAt; //
}
