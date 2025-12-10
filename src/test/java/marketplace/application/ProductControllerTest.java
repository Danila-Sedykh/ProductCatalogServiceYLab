package marketplace.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import marketplace.domain.Product;
import marketplace.dto.CreateProductRequest;
import marketplace.dto.ProductDto;
import marketplace.in.controllers.ProductController;
import marketplace.mapper.ProductMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ProductControllerTest {

    private ProductService productService;
    private ProductMapper productMapper;
    private ProductController productController;
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        productService = mock(ProductService.class);
        productMapper = mock(ProductMapper.class);
        productController = new ProductController(productService, productMapper);
        mockMvc = MockMvcBuilders.standaloneSetup(productController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void createProduct_ok() throws Exception {
        CreateProductRequest request = new CreateProductRequest();
        request.setCode(100L);
        request.setName("Phone");

        Product product = new Product();
        product.setCode(request.getCode());
        product.setName(request.getName());

        ProductDto dto = new ProductDto();
        dto.setCode(request.getCode());
        dto.setName(request.getName());

        when(productMapper.toEntity(request)).thenReturn(product);
        when(productMapper.toDto(product)).thenReturn(dto);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(100))
                .andExpect(jsonPath("$.name").value("Phone"));

        verify(productService, times(1)).addProduct(product);
    }

    @Test
    void getProductById_found() throws Exception {
        Product product = new Product();
        product.setId(1L);
        product.setCode(100L);
        product.setName("Phone");

        ProductDto dto = new ProductDto();
        dto.setId(1L);
        dto.setCode(100L);
        dto.setName("Phone");

        when(productService.getById(1L)).thenReturn(Optional.of(product));
        when(productMapper.toDto(product)).thenReturn(dto);

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.code").value(100))
                .andExpect(jsonPath("$.name").value("Phone"));
    }

    @Test
    void getProductById_notFound() throws Exception {
        when(productService.getById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteProduct_ok() throws Exception {
        when(productService.deleteProduct(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isNoContent());

        verify(productService).deleteProduct(1L);
    }

    @Test
    void deleteProduct_notFound() throws Exception {
        when(productService.deleteProduct(1L)).thenReturn(false);

        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isNotFound());

        verify(productService).deleteProduct(1L);
    }
}