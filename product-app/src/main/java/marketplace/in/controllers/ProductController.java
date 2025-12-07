package marketplace.in.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import marketplace.application.ProductService;
import marketplace.domain.Product;
import marketplace.dto.CreateProductRequest;
import marketplace.dto.ProductDto;
import marketplace.dto.UpdateProductRequest;
import marketplace.mapper.ProductMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
@Tag(name = "Products", description = "Управление товарами в каталоге")
public class ProductController {
    private final ProductService productService;
    private final ProductMapper productMapper;

    public ProductController(ProductService productService, ProductMapper productMapper) {
        this.productService = productService;
        this.productMapper = productMapper;
    }

    @PostMapping
    @Operation(summary = "Создать новый товар")
    @ApiResponse(responseCode = "201", description = "Товар успешно создан")
    @ApiResponse(responseCode = "400", description = "Некорректные данные", content = @Content)
    public ResponseEntity<ProductDto> createProduct(@Valid @RequestBody CreateProductRequest request) {
        Product product = productMapper.toEntity(request);
        productService.addProduct(product);
        ProductDto dto = productMapper.toDto(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @PutMapping
    @Operation(summary = "Обновить существующий товар")
    @ApiResponse(responseCode = "200", description = "Товар обновлён")
    @ApiResponse(responseCode = "404", description = "Товар не найден", content = @Content)
    public ResponseEntity<Void> updateProduct(@Valid @RequestBody UpdateProductRequest request) {
        Product product = new marketplace.domain.Product();
        product.setId(request.getId());
        product.setCode(request.getCode());
        product.setName(request.getName());
        product.setCategory(request.getCategory());
        product.setBrand(request.getBrand());
        product.setPrice(request.getPrice());

        boolean updated = productService.updateProduct(product);
        if (updated) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить товар по ID")
    @ApiResponse(responseCode = "204", description = "Товар удалён")
    @ApiResponse(responseCode = "404", description = "Товар не найден", content = @Content)
    public ResponseEntity<Void> deleteProduct(
            @Parameter(description = "ID товара", required = true)
            @PathVariable("id") Long id) {
        boolean deleted = productService.deleteProduct(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить товар по ID")
    @ApiResponse(responseCode = "200", description = "Товар найден", content = @Content(schema = @Schema(implementation = ProductDto.class)))
    @ApiResponse(responseCode = "404", description = "Товар не найден", content = @Content)
    public ResponseEntity<ProductDto> getProductById(
            @Parameter(description = "ID товара", required = true)
            @PathVariable("id") Long id) {
        Optional<Product> productOpt = productService.getById(id);
        return productOpt
                .map(p -> ResponseEntity.ok(productMapper.toDto(p)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "Поиск товаров с фильтрацией")
    @ApiResponse(responseCode = "200", description = "Список товаров", content = @Content(schema = @Schema(implementation = ProductDto.class)))
    public ResponseEntity<List<ProductDto>> searchProducts(
            @RequestParam(name = "category", required = false) String category,
            @RequestParam(name = "brand",required = false) String brand,
            @RequestParam(name = "name",required = false) String name,
            @RequestParam(name = "minPrice",required = false) BigDecimal minPrice,
            @RequestParam(name = "maxPrice",required = false) BigDecimal maxPrice) {

        Map<String, String> criteria = new HashMap<>();
        if (category != null && !category.trim().isEmpty()) criteria.put("category", category);
        if (brand != null && !brand.trim().isEmpty()) criteria.put("brand", brand);
        if (name != null && !name.trim().isEmpty()) criteria.put("name", name);

        List<Product> products = productService.search(criteria, minPrice, maxPrice);
        List<ProductDto> dtos = products.stream()
                .map(productMapper::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }
}
