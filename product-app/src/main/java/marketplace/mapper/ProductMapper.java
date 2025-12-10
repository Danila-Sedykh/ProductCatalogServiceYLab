package marketplace.mapper;

import marketplace.domain.Product;
import marketplace.dto.CreateProductRequest;
import marketplace.dto.ProductDto;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    ProductMapper INSTANCE = Mappers.getMapper(ProductMapper.class);

    ProductDto toDto(Product product);
    Product toEntity(CreateProductRequest request);
}
