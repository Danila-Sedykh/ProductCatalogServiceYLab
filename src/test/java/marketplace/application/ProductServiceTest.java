package marketplace.application;

import marketplace.db.DataSourceFactory;
import marketplace.db.LiquibaseRunner;
import marketplace.domain.Product;
import marketplace.out.cache.LruCache;
import marketplace.out.repository.ProductRepositoryJdbc;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;


public class ProductServiceTest {
    private Path tempFile;
    private ProductService service;
    private ProductRepositoryJdbc repo;
    private static final String APP_SCHEMA = "marketplace";
    private DataSource dataSource = DataSourceFactory.create(
            "jdbc:postgresql://localhost:54432/db_y_lab",
            "user_123",
            "pass_123",
            5
    );

    @BeforeEach
    void setUp() throws Exception {
        LiquibaseRunner.runLiquibase(
                dataSource,
                "db/changelog/db-changelog-master.xml",
                "liquibase_schema",
                APP_SCHEMA
        );
        tempFile = Files.createTempFile("products",".db");
        repo = new ProductRepositoryJdbc(dataSource, APP_SCHEMA);
        service = new ProductService(repo, new LruCache<>(10));
        Product p1 = new Product();
        p1.setCode(1L);
        p1.setName("Product 1");
        p1.setBrand("BrandAA");
        p1.setCategory("Electronics");
        p1.setPrice(100.0);

        Product p2 = new Product();
        p2.setCode(2L);
        p2.setName("Product 2");
        p2.setBrand("BrandB");
        p2.setCategory("Books");
        p2.setPrice(50.0);
        service.addProduct(p1);
        service.addProduct(p2);
    }

    @AfterEach
    void tearDown() throws Exception { Files.deleteIfExists(tempFile); }

    @Test
    void searchByBrand() {
        List<Product> res = service.search(java.util.Map.of("brand", "BrandAA"), null, null);
        assertThat(res).hasSize(1);
        assertThat(res.get(0).getCode()).isEqualTo(1L);
    }

}
