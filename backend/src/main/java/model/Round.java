package model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "ROUND")
public class Round extends PanacheEntityBase {
    @Id
    @GeneratedValue
    @Column(name = "ROUND_ID")
    public UUID roundId; //

    @ManyToOne
    @JoinColumn(name = "SESSION_ID", nullable = false)
    public GameSession session; // [cite: 96]

    @Column(name = "ROUND_NUMBER", nullable = false)
    public Integer roundNumber; //

    @ManyToOne
    @JoinColumn(name = "BARCODE", nullable = false)
    public Product product; // [cite: 97]

    @Column(name = "GUESSED_MIN")
    public Integer guessedMin; //

    @Column(name = "GUESSED_MAX")
    public Integer guessedMax; //

    @Column(name = "ACTUAL_KCAL")
    public Integer actualKcal; //

    @Column(name = "POINTS")
    public Integer points = 0; //
}
