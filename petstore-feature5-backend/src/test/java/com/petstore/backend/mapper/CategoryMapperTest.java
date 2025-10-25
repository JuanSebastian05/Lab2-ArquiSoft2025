package com.petstore.backend.mapper;

import com.petstore.backend.dto.CategoryDTO;
import com.petstore.backend.entity.Category;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CategoryMapperTest {

    private final CategoryMapper mapper = CategoryMapper.INSTANCE;

    private Category category(Integer id, String name, String desc) {
        Category c = new Category();
        c.setCategoryId(id);
        c.setCategoryName(name);
        c.setDescription(desc);
        return c;
        }

    @Test
    @DisplayName("toDTO mapea todos los campos básicos")
    void toDTO_mapsAll() {
        Category entity = category(10, "Perros", "Accesorios caninos");

        CategoryDTO dto = mapper.toDTO(entity);

        assertThat(dto.getCategoryId()).isEqualTo(10);
        assertThat(dto.getCategoryName()).isEqualTo("Perros");
        assertThat(dto.getDescription()).isEqualTo("Accesorios caninos");
    }

    @Test
    @DisplayName("toEntity mapea todos los campos básicos")
    void toEntity_mapsAll() {
        CategoryDTO dto = new CategoryDTO(20, "Gatos", "Juguetes felinos");

        Category entity = mapper.toEntity(dto);

        assertThat(entity.getCategoryId()).isEqualTo(20);
        assertThat(entity.getCategoryName()).isEqualTo("Gatos");
        assertThat(entity.getDescription()).isEqualTo("Juguetes felinos");
    }

    @Test
    @DisplayName("toDTOList mapea listas de entidades a DTOs")
    void toDTOList_mapsList() {
        List<Category> entities = List.of(
                category(1, "A", "d1"),
                category(2, "B", "d2")
        );
        List<CategoryDTO> dtos = mapper.toDTOList(entities);
        assertThat(dtos).hasSize(2);
        assertThat(dtos.get(0).getCategoryId()).isEqualTo(1);
        assertThat(dtos.get(1).getCategoryName()).isEqualTo("B");
    }

    @Test
    @DisplayName("toEntityList mapea listas de DTOs a entidades")
    void toEntityList_mapsListBack() {
        List<CategoryDTO> dtos = List.of(
                new CategoryDTO(3, "C", "d3"),
                new CategoryDTO(4, "D", "d4")
        );
        List<Category> entities = mapper.toEntityList(dtos);
        assertThat(entities).hasSize(2);
        assertThat(entities.get(0).getCategoryId()).isEqualTo(3);
        assertThat(entities.get(1).getCategoryName()).isEqualTo("D");
    }
}
