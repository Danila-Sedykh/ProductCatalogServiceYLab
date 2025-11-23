package marketplace.application;

import marketplace.application.port.*;
import marketplace.db.DataSourceFactory;
import marketplace.db.LiquibaseRunner;
import marketplace.domain.Product;
import marketplace.out.cache.LruCache;
import marketplace.out.repository.ProductRepositoryJdbc;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.mockito.*;

import javax.sql.DataSource;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class ProductServiceTest {
    @Mock
    private ProductRepository productRepository;

    private LruCache<String, List<Product>> cache;
    private ProductService productService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        cache = new LruCache<>(10);
        productService = new ProductService(productRepository, cache);
    }

    @Test
    @DisplayName("должен добавить продукт и аннулировать кэш")
    void addProduct() {
        Product product = new Product();
        product.setCode(123L);
        product.setName("Test");

        productService.addProduct(product);

        verify(productRepository).save(product);
        Assertions.assertThat(cache.get("any")).isNull();
    }

    @Test
    @DisplayName("Должен вернуть продукт по Id")
    void getById() {
        Product product = new Product();
        product.setId(1L);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        Optional<Product> result = productService.getById(1L);

        Assertions.assertThat(result).contains(product);
    }

    @Test
    @DisplayName("должен удалить продукт")
    void deleteProduct() {
        when(productRepository.delete(1L)).thenReturn(true);

        boolean deleted = productService.deleteProduct(1L);

        verify(productRepository).delete(1L);
        Assertions.assertThat(deleted).isTrue();
    }

}
