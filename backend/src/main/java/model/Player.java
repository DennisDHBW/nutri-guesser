package model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "PLAYER", schema = "PUBLIC")
public class Player extends PanacheEntityBase {

    @Id
    @GeneratedValue
    @Column(name = "PLAYER_ID")
    public UUID playerId;

    @Column(name = "NICKNAME", nullable = false, length = 50)
    public String nickname;

    @Column(name = "CREATED_AT", insertable = false, updatable = false)
    public LocalDateTime createdAt;
}
