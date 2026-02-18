package model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "GAME_SESSION")
public class GameSession extends PanacheEntityBase {
    @Id
    @GeneratedValue
    @Column(name = "SESSION_ID")
    public UUID sessionId;

    @ManyToOne
    @JoinColumn(name = "PLAYER_ID", nullable = false)
    public Player player;

    @Column(name = "STARTED_AT", nullable = false)
    public LocalDateTime startedAt;

    @Column(name = "ENDED_AT")
    public LocalDateTime endedAt;

    @Column(name = "TOTAL_SCORE", nullable = false)
    public Integer totalScore = null;
}
