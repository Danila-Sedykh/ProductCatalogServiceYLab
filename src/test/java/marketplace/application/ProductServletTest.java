package marketplace.application;

import com.fasterxml.jackson.databind.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import marketplace.application.port.ProductRepository;
import marketplace.aspect.*;
import marketplace.domain.Product;
import marketplace.dto.*;
import marketplace.in.servlet.*;
import marketplace.out.cache.LruCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;

import java.io.*;
import java.math.*;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServletTest {
    @InjectMocks
    private ProductServlet productServlet;

    @Mock
    private ProductService productService;

    @Mock
    private AuditAspect auditAspect;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private ServletInputStream inputStream;

    @Test
    @DisplayName("должен обновить продукт и вернуть 200")
    void doPut_ok() throws Exception {
        UpdateProductRequest updateReq = new UpdateProductRequest();
        updateReq.setId(1L);
        updateReq.setCode(12L);
        updateReq.setName("Phone");
        updateReq.setCategory("Electronics");
        updateReq.setBrand("Samsung");
        updateReq.setPrice(BigDecimal.valueOf(999));

        when(request.getInputStream()).thenReturn(inputStream);
        when(objectMapper.readValue(inputStream, UpdateProductRequest.class))
                .thenReturn(updateReq);

        when(productService.updateProduct(any(Product.class))).thenReturn(true);
        productServlet.doPut(request, response);

        verify(auditAspect).setRequest(request);

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productService).updateProduct(captor.capture());

        Product sentProduct = captor.getValue();
        assertThat(sentProduct.getId()).isEqualTo(1L);
        assertThat(sentProduct.getCode()).isEqualTo(12L);
        assertThat(sentProduct.getName()).isEqualTo("Phone");
        assertThat(sentProduct.getCategory()).isEqualTo("Electronics");
        assertThat(sentProduct.getBrand()).isEqualTo("Samsung");
        assertThat(sentProduct.getPrice()).isEqualTo(999.0);

        verify(response).setStatus(HttpServletResponse.SC_OK);
        verify(auditAspect).clearRequest();
    }

    @Test
    @DisplayName("должен создать продукт и вернуть 201")
    void doPost_ok() throws Exception {
        CreateProductRequest createReq = new CreateProductRequest();
        createReq.setCode(123L);
        createReq.setName("Phone");
        createReq.setCategory("Electronics");
        createReq.setBrand("Samsung");
        createReq.setPrice(BigDecimal.valueOf(999));

        when(request.getInputStream()).thenReturn(inputStream);
        when(objectMapper.readValue(inputStream, CreateProductRequest.class))
                .thenReturn(createReq);

        doAnswer(invocation -> {
            Product p = invocation.getArgument(0);
            p.setId(1L);
            return null;
        }).when(productService).addProduct(any(Product.class));

        productServlet.doPost(request, response);

        verify(auditAspect).setRequest(request);
        verify(productService).addProduct(any(Product.class));
        verify(response).setStatus(HttpServletResponse.SC_CREATED);
        verify(auditAspect).clearRequest();
    }

    @Test
    @DisplayName("должен вернуть товар по id и 200")
    void doGet_ok() throws Exception {
        Product product = new Product();
        product.setId(1L);
        product.setCode(123L);
        product.setName("Phone");

        when(request.getPathInfo()).thenReturn("/1");
        when(productService.getById(1L)).thenReturn(Optional.of(product));

        productServlet.doGet(request, response);
        verify(response).setStatus(HttpServletResponse.SC_OK);
        verify(response).getWriter();
    }

    @Test
    @DisplayName("должен удалить продукт и вернуть 204")
    void doDelete_ok() throws Exception {
        when(request.getPathInfo()).thenReturn("/1");
        when(productService.deleteProduct(1L)).thenReturn(true);

        productServlet.doDelete(request, response);

        verify(auditAspect).setRequest(request);
        verify(productService).deleteProduct(1L);
        verify(response).setStatus(HttpServletResponse.SC_NO_CONTENT);
        verify(auditAspect).clearRequest();
    }
}
