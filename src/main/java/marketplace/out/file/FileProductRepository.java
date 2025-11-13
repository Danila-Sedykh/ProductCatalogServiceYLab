package marketplace.out.file;

import marketplace.domain.Product;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;


/**
 * Репозиторий, который хранит товары в файле products.db.
 */

public class FileProductRepository {
    private final Path file;
    private final Map<Long, Product> productStorage = new LinkedHashMap<>();

    public FileProductRepository(Path file) {
        this.file = file;
        load();
    }

    private void load() {
        if (!Files.exists(file)) return;
        try (BufferedReader reader = Files.newBufferedReader(file)) {
            reader.lines().forEach(line -> {
                String[] parts = line.split(";");
                if (parts.length < 5) return;
                Long id = Long.valueOf(parts[0]);
                String name = parts[1];
                String category = parts[2];
                String brand = parts[3];
                double price = Double.parseDouble(parts[4].replace(",", "."));
                productStorage.put(id, new Product(id, name, category, brand, price));
            });
        } catch (IOException e) {
            throw new RuntimeException("Failed to load products: " + e.getMessage(), e);
        }
    }

    private void persist() {
        try (BufferedWriter writer = Files.newBufferedWriter(file)) {
            for (Product p : productStorage.values()) {
                writer.write(String.format("%s;%s;%s;%s;%.2f%n",
                        p.getId(), p.getName(), p.getCategory(), p.getBrand(), p.getPrice()));
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to save products: " + e.getMessage(), e);
        }
    }

    public List<Product> findAll() {
        return new ArrayList<>(productStorage.values());
    }

    public Optional<Product> findById(Long id) {
        return Optional.ofNullable(productStorage.get(id));
    }

    public void save(Product product) {
        productStorage.put(product.getId(), product);
        persist();
    }

    public boolean update(Long id, Product update){
        if (!productStorage.containsKey(id)) {
            return false;
        }
        productStorage.put(id, update);
        persist();
        return true;
    }

    public boolean delete(Long id) {
        if (!productStorage.containsKey(id)) {
            return false;
        }
        productStorage.remove(id);
        persist();
        return true;
    }
}
