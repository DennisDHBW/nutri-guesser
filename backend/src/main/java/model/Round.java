package model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "ROUND")
public class Round extends PanacheEntityBase {
    @Id
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

    @Column(name = "GUESSED_RANGE")
    public String guessedRange; //

    @Column(name = "ACTUAL_KCAL")
    public Integer actualKcal; //

    @Column(name = "POINTS")
    public Integer points; //
}
