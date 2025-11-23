package marketplace.application;

import marketplace.application.port.ProductRepository;
import marketplace.aspect.Auditable;
import marketplace.domain.Product;
import marketplace.out.cache.LruCache;
import marketplace.out.repository.ProductRepositoryJdbc;


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
    private final ProductRepository productRepository;
    private final LruCache<String, List<Product>> cache;

    public ProductService(ProductRepository productRepository, LruCache<String, List<Product>> cache){
        this.productRepository = productRepository;
        this.cache = cache;
    }

    @Auditable(action = "ADD_PRODUCT")
    public void addProduct(Product product) {
        productRepository.save(product);
        cache.invalidateAll();
    }

    @Auditable(action = "UPDATE_PRODUCT")
    public boolean updateProduct(Product updated) {
        boolean result = productRepository.update(updated);
        if (result) cache.invalidateAll();
        return result;
    }

    @Auditable(action = "DELETE_PRODUCT")
    public boolean deleteProduct(Long id) {
        boolean result = productRepository.delete(id);
        if (result) cache.invalidateAll();
        return result;
    }

    @Auditable(action = "SEARCH_BY_ID_PRODUCTS")
    public Optional<Product> getById(Long id) {
        return productRepository.findById(id);
    }

    @Auditable(action = "SEARCH_PRODUCTS")
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

    @Auditable(action = "COUNT_PRODUCTS")
    public int count() { return productRepository.findAll().size(); }

}
