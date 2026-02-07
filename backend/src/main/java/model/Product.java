package model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

@Entity
@Table(name = "PRODUCT", schema = "PUBLIC")
public class Product extends PanacheEntityBase {

    @Id
    @Column(name = "BARCODE", length = 50)
    public String barcode;

    @Column(name = "NAME", nullable = false, length = 150)
    public String name;

    @Column(name = "BRAND", length = 150)
    public String brand;

    @Column(name = "IMAGE_URL", length = 500)
    public String imageUrl;

    @OneToOne(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    public NutritionFacts nutritionFacts;
}