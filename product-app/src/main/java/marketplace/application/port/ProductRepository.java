package marketplace.application.port;

import marketplace.domain.Product;

import java.sql.ResultSet;
import java.util.List;
import java.util.Optional;

public interface ProductRepository {
    Product save(Product p);
    boolean update(Product p);
    boolean delete(long id);
    Optional<Product> findById(long id);
    List<Product> findAll();
}
