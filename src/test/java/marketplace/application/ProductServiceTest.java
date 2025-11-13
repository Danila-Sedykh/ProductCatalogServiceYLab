package marketplace.application;

import marketplace.domain.Product;
import marketplace.out.cache.LruCache;
import marketplace.out.file.FileProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;


public class ProductServiceTest {
    private Path tempFile;
    private FileProductRepository repo;
    private ProductService service;

    @BeforeEach
    void setUp() throws Exception {
        tempFile = Files.createTempFile("products",".db");
        repo = new FileProductRepository(tempFile);
        service = new ProductService(repo, new LruCache<>(10));
        service.addProduct(new Product("1", "Phone X", "Electronics", "BrandA", 499));
        service.addProduct(new Product("2", "Phone Y", "Electronics", "BrandB", 299));
    }

    @AfterEach
    void tearDown() throws Exception { Files.deleteIfExists(tempFile); }

    @Test
    void searchByBrand() {
        List<Product> res = service.search(java.util.Map.of("brand", "branda"), null, null);
        assertThat(res).hasSize(1);
        assertThat(res.get(0).getId()).isEqualTo("1");
    }

}
