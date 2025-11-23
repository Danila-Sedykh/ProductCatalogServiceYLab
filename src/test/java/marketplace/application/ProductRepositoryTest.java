package marketplace.application;

import marketplace.config.*;
import marketplace.db.DataSourceFactory;
import marketplace.db.LiquibaseRunner;
import marketplace.domain.Product;
import marketplace.out.repository.ProductRepositoryJdbc;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.containers.PostgreSQLContainer;

import javax.sql.DataSource;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ProductRepositoryTest {
    static PostgreSQLContainer<?> pg = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("db_y_lab")
            .withUsername("user_123")
            .withPassword("pass_123");

    private DataSource ds;
    private ProductRepositoryJdbc repo;

    @BeforeAll
    void startDb() {
        pg.start();
        String jdbc = pg.getJdbcUrl();
        ds = DataSourceFactory.create(jdbc, pg.getUsername(), pg.getPassword(), Integer.parseInt(ConfigLoader.get("db.maximumPoolSize")));
        LiquibaseRunner.runLiquibase(ds, ConfigLoader.get("liquibase.changelog"),ConfigLoader.get("db.liquibaseSchema"),  ConfigLoader.get("db.appSchema"));
        repo = new ProductRepositoryJdbc(ds, ConfigLoader.get("db.appSchema"));
    }

    @AfterAll
    void stopDb() { pg.stop(); }

    private Product createTestProduct() {
        Product p = new Product();
        p.setCode(new Random().nextLong(1, 1_000));
        p.setName("Test Product " + UUID.randomUUID());
        p.setBrand("BrandA");
        p.setCategory("Electronics");
        p.setPrice(123.45);
        return p;
    }


    @Test
    void save() {
        Product p = createTestProduct();
        repo.save(p);
        assertThat(p.getId()).isGreaterThan(0);
    }

    @Test
    void update() {
        Product p = createTestProduct();
        repo.save(p);

        p.setName("Updated Name");
        p.setPrice(999.99);

        boolean updated = repo.update(p);
        assertThat(updated).isTrue();

    }

    @Test
    void delete() {
        Product p = createTestProduct();
        repo.save(p);

        boolean deleted = repo.delete(p.getId());
        assertThat(deleted).isTrue();
    }

    @Test
    void findById() {
        Optional<Product> found = repo.findById(1L);
        assertThat(found).isEmpty();
    }

    @Test
    void findAll() {
        Product p1 = createTestProduct();
        Product p2 = createTestProduct();
        repo.save(p1);
        repo.save(p2);

        List<Product> all = repo.findAll();
        assertThat(all).contains(p1, p2);
    }
}
