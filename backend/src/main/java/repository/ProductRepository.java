package repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import model.Product;

@ApplicationScoped
public class ProductRepository implements PanacheRepositoryBase<Product, String> {

    public Product findRandomFromDb() {
        return find("ORDER BY rand()").firstResult();
    }
}