package repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import model.Product;
import java.util.List;

@ApplicationScoped
public class ProductRepository implements PanacheRepositoryBase<Product, String> {

    // Suche alle Produkte einer bestimmten Marke
    public List<Product> listByBrand(String brand) {
        return list("brand", brand);
    }
}