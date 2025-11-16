package marketplace.application;

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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
        ds = DataSourceFactory.create(jdbc, pg.getUsername(), pg.getPassword(), 5);
        LiquibaseRunner.runLiquibase(ds, "db/changelog/db-changelog-master.xml","liquibase_schema",  "marketplace");
        repo = new ProductRepositoryJdbc(ds, "marketplace");
    }

    @AfterAll
    void stopDb() { pg.stop(); }

    @Test
    void saveAndFind() {
        Product p = new Product();
        p.setCode(10L);
        p.setName("Test Product");
        p.setBrand("BrandA");
        p.setCategory("Electronics");
        p.setPrice(123.45);
        repo.save(p);
        assertThat(p.getId()).isGreaterThan(0);
        List<Product> all = repo.findAll();
        assertThat(all).isNotEmpty();
    }
}
