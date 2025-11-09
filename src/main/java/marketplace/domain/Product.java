package marketplace.domain;

import java.util.Objects;

public class Product {
    private final String id;
    private String name;
    private String category;
    private String brand;
    private double price;

    public Product(String id, String name, String category, String brand, double price) {
        this.id = Objects.requireNonNull(id, "Не может быть null");
        this.name = name;
        this.category = category;
        this.brand = brand;
        this.price = price;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    @Override
    public boolean equals(Object o){
        if(this == o) {
            return true;
        }
        if(!(o instanceof Product)) {
            return false;
        }
        Product product = (Product) o;
        return id.equals(product.id);
    }

    @Override
    public int hashCode(){
        return id.hashCode();
    }

    @Override
    public String toString(){
        return String.format("id = '%s', name = '%s', category = '%s', brand = '%s', prise = %.2f",
                id, name, category, brand, price);
    }

}
