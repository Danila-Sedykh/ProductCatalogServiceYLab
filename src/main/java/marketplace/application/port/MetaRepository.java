package marketplace.application.port;

import java.util.List;

public interface MetaRepository {
    List<String> listCategories();
    void addCategory(String name);
    void removeCategory(String name);
    List<String> listBrands();
    void addBrand(String name);
    void removeBrand(String name);
}
