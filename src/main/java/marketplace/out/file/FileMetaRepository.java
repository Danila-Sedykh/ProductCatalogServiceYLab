package marketplace.out.file;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;


/**
 * Хранит метаданные: категории и бренды в meta.db
 */

public class FileMetaRepository {
    private final Path file;
    private final Set<String> categories = new TreeSet<>();
    private final Set<String> brands = new TreeSet<>();

    public FileMetaRepository(Path file) {
        this.file = file;
        load();
    }

    private void load() {
        if (!Files.exists(file)) return;
        try (BufferedReader reader = Files.newBufferedReader(file)) {
            Set<String> current = null;
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                if (line.equalsIgnoreCase("# categories")) {
                    current = categories;
                    continue;
                }
                if (line.equalsIgnoreCase("# brands")) {
                    current = brands;
                    continue;
                }
                if (current != null) current.add(line);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load meta data: " + e.getMessage(), e);
        }
    }

    private void persist() {
        try (BufferedWriter writer = Files.newBufferedWriter(file)) {
            writer.write("# categories\n");
            for (String c : categories) writer.write(c + "\n");
            writer.write("# brands\n");
            for (String b : brands) writer.write(b + "\n");
        } catch (IOException e) {
            throw new RuntimeException("Failed to save meta data: " + e.getMessage(), e);
        }
    }


    public List<String> listCategories() {
        return new ArrayList<>(categories);
    }

    public void addCategory(String category) {
        if (category == null || category.isBlank()) return;
        categories.add(category.trim());
        persist();
    }

    public void removeCategory(String category) {
        categories.remove(category);
        persist();
    }

    public List<String> listBrands() {
        return new ArrayList<>(brands);
    }

    public void addBrand(String brand) {
        if (brand == null || brand.isBlank()) return;
        brands.add(brand.trim());
        persist();
    }

    public void removeBrand(String brand) {
        brands.remove(brand);
        persist();
    }

}
