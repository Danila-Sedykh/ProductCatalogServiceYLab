package marketplace.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class UpdateProductRequest {
    @NotNull
    private Long id;
    private Long code;
    private String name;
    private String category;
    private String brand;
    private BigDecimal price;

    public Long getId() {
        return id;
    }

    public Long getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public String getBrand() {
        return brand;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setCode(Long code) {
        this.code = code;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
