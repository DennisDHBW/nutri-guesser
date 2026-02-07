package model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

@Entity
@Table(name = "NUTRITION_FACTS")
public class NutritionFacts extends PanacheEntityBase {
    @Id
    @Column(name = "BARCODE")
    public String barcode;

    @Column(name = "KCAL_100G", nullable = false)
    public float kcal100g;

    /*
    @Column(name = "FAT_100G")
    public float fat100g;

    @Column(name = "CARBS_100G")
    public float carbs100g;

    @Column(name = "PROTEIN_100G")
    public float protein100g;
     */

    @OneToOne
    @JoinColumn(name = "BARCODE")
    @MapsId
    public Product product;
}
