package marketplace.application;

import marketplace.domain.Product;
import marketplace.out.cache.LruCache;
import marketplace.out.file.FileProductRepository;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Сервис управления товарами. Зависимости инверсированы — репозитории передаются извне.
 */

public class ProductService {
    private final FileProductRepository productRepository;
    private final LruCache<String, List<Product>> cache;

    public ProductService(FileProductRepository productRepository, LruCache<String, List<Product>> cache){
        this.productRepository = productRepository;
        this.cache = cache;
    }

    public void addProduct(Product product) {
        productRepository.save(product);
        cache.invalidateAll();
    }

    public boolean updateProduct(Long id, Product updated) {
        boolean result = productRepository.update(id, updated);
        if (result) cache.invalidateAll();
        return result;
    }

    public boolean deleteProduct(Long id) {
        boolean result = productRepository.delete(id);
        if (result) cache.invalidateAll();
        return result;
    }

    public Optional<Product> getById(Long id) {
        return productRepository.findById(id);
    }

    public List<Product> search(Map<String, String> criteria, Double minPrice, Double maxPrice) {
        String cacheKey = buildCacheKey(criteria, minPrice, maxPrice);
        List<Product> cached = cache.get(cacheKey);
        if (cached != null) return cached;

        Predicate<Product> predicate = p -> true;
        if (criteria != null) {
            for (Map.Entry<String, String> e : criteria.entrySet()) {
                String k = e.getKey().toLowerCase(Locale.ROOT);
                String v = e.getValue().toLowerCase(Locale.ROOT);
                switch (k) {
                    case "category": predicate = predicate.and(p -> p.getCategory() != null && p.getCategory().toLowerCase(Locale.ROOT).contains(v)); break;
                    case "brand": predicate = predicate.and(p -> p.getBrand() != null && p.getBrand().toLowerCase(Locale.ROOT).contains(v)); break;
                    case "name": predicate = predicate.and(p -> p.getName() != null && p.getName().toLowerCase(Locale.ROOT).contains(v)); break;
                    default: break;
                }
            }
        }

        if (minPrice != null) predicate = predicate.and(p -> p.getPrice() >= minPrice);
        if (maxPrice != null) predicate = predicate.and(p -> p.getPrice() <= maxPrice);

        List<Product> result = productRepository.findAll().stream().filter(predicate).collect(Collectors.toList());
        cache.put(cacheKey, result);
        return result;
    }

    private String buildCacheKey(Map<String, String> criteria, Double minPrice, Double maxPrice) {
        StringBuilder sb = new StringBuilder();
        if (criteria != null) {
            criteria.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(e -> sb.append(e.getKey()).append("=").append(e.getValue()).append(";"));
        }
        sb.append("min=").append(minPrice).append(";max=").append(maxPrice);
        return sb.toString();
    }

    public int count() { return productRepository.findAll().size(); }

}
