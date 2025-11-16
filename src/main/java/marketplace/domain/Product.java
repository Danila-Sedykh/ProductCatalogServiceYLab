package marketplace.domain;


public class Product {
    private Long id;
    private Long code;
    private String name;
    private String category;
    private String brand;
    private double price;



    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCode() {
        return code;
    }

    public void setCode(Long code) {
        this.code = code;
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
        return code.equals(product.code);
    }

    @Override
    public int hashCode(){
        return code.hashCode();
    }

    @Override
    public String toString(){
        return String.format("id = '%s', name = '%s', category = '%s', brand = '%s', prise = %.2f",
                code, name, category, brand, price);
    }

}
