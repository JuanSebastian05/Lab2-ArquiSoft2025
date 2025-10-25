package com.petstore.backend.mapper;

import com.petstore.backend.dto.CategoryDTO;
import com.petstore.backend.dto.ProductDTO;
import com.petstore.backend.entity.Category;
import com.petstore.backend.entity.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ProductMapperTest {

    private final ProductMapper mapper = ProductMapper.INSTANCE;

    private Category category(int id, String name) {
        Category c = new Category();
        c.setCategoryId(id);
        c.setCategoryName(name);
        return c;
    }

    private Product product(int id, String name, double basePrice, Integer categoryId) {
        Product p = new Product();
        p.setProductId(id);
        p.setProductName(name);
        p.setBasePrice(basePrice);
        if (categoryId != null) {
            p.setCategory(category(categoryId, "Cat" + categoryId));
        }
        return p;
    }

    @Test
    @DisplayName("toDTO mapea basePrice->price y category anidado")
    void toDTO_mapsPriceAndCategory() {
        Product entity = product(10, "Collar", 25.5, 1);

        ProductDTO dto = mapper.toDTO(entity);

        assertThat(dto.getProductId()).isEqualTo(10);
        assertThat(dto.getProductName()).isEqualTo("Collar");
        // Comparar BigDecimal por valor
        assertThat(dto.getPrice()).isNotNull();
        assertThat(dto.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(25.5));
        // Campos ignorados por el mapper quedan null
        assertThat(dto.getDescription()).isNull();
        assertThat(dto.getStatus()).isNull();
        // Category se mapea por propiedades
        CategoryDTO cat = dto.getCategory();
        assertThat(cat).isNotNull();
        assertThat(cat.getCategoryId()).isEqualTo(1);
        assertThat(cat.getCategoryName()).isEqualTo("Cat1");
    }

    @Test
    @DisplayName("toEntity mapea price(BigDecimal)->basePrice y omite productId")
    void toEntity_mapsBack() {
        ProductDTO dto = new ProductDTO();
        dto.setProductId(99); // se ignora en el mapping a entidad
        dto.setProductName("Correa");
        dto.setPrice(BigDecimal.valueOf(30.0));

        Product entity = mapper.toEntity(dto);

        assertThat(entity.getProductId()).isNull(); // ignorado
        assertThat(entity.getProductName()).isEqualTo("Correa");
        assertThat(entity.getBasePrice()).isEqualTo(30.0);
    }

    @Test
    @DisplayName("toDTOList mapea listas")
    void toDTOList_mapsList() {
        List<Product> list = List.of(product(1, "A", 10.0, null), product(2, "B", 20.0, null));
        List<ProductDTO> dtos = mapper.toDTOList(list);
        assertThat(dtos).hasSize(2);
        assertThat(dtos.get(0).getProductId()).isEqualTo(1);
        assertThat(dtos.get(1).getPrice()).isEqualByComparingTo("20.0");
    }
}
