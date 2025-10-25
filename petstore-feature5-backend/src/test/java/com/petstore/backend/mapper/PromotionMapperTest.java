package com.petstore.backend.mapper;

import com.petstore.backend.dto.PromotionDTO;
import com.petstore.backend.entity.Category;
import com.petstore.backend.entity.Promotion;
import com.petstore.backend.entity.Status;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class PromotionMapperTest {

    private final PromotionMapper mapper = PromotionMapper.INSTANCE;

    private Status status(String name) {
        Status s = new Status();
        s.setStatusName(name);
        return s;
    }

    private Category category(int id, String name) {
        Category c = new Category();
        c.setCategoryId(id);
        c.setCategoryName(name);
        return c;
    }

    @Test
    @DisplayName("toDTO mapea discount(Double)->BigDecimal y LocalDate->LocalDateTime")
    void toDTO_mapsCoreFields() {
        Promotion entity = new Promotion();
        entity.setPromotionId(5);
        entity.setPromotionName("Promo");
        entity.setDescription("Desc");
        entity.setDiscountValue(15.0);
        entity.setStartDate(LocalDate.of(2025, 1, 10));
        entity.setEndDate(LocalDate.of(2025, 2, 10));
        entity.setStatus(status("ACTIVE"));
        entity.setCategory(category(3, "Perros"));

        PromotionDTO dto = mapper.toDTO(entity);

        assertThat(dto.getPromotionId()).isEqualTo(5);
        assertThat(dto.getPromotionName()).isEqualTo("Promo");
        assertThat(dto.getDescription()).isEqualTo("Desc");
        assertThat(dto.getDiscountPercentage()).isEqualByComparingTo(BigDecimal.valueOf(15.0));
        assertThat(dto.getStartDate()).isEqualTo(LocalDateTime.of(2025,1,10,0,0));
        // Nota: el mapper configura endDate con atStartOfDay
        assertThat(dto.getEndDate()).isEqualTo(LocalDateTime.of(2025,2,10,0,0));
        assertThat(dto.getStatus()).isEqualTo("ACTIVE");
        assertThat(dto.getCategory()).isNotNull();
        assertThat(dto.getCategory().getCategoryId()).isEqualTo(3);
        // product se ignora
        assertThat(dto.getProduct()).isNull();
    }

    @Test
    @DisplayName("toEntity mapea BigDecimal->Double y LocalDateTime->LocalDate, ignora status y user")
    void toEntity_mapsBack() {
        PromotionDTO dto = new PromotionDTO();
        dto.setPromotionId(99); // ignorado al mapear a entidad
        dto.setPromotionName("Promo2");
        dto.setDescription("D2");
        dto.setDiscountPercentage(BigDecimal.valueOf(7.5));
        dto.setStartDate(LocalDateTime.of(2025,3,1,0,0));
        dto.setEndDate(LocalDateTime.of(2025,3,31,0,0));

        Promotion entity = mapper.toEntity(dto);

        assertThat(entity.getPromotionId()).isNull();
        assertThat(entity.getPromotionName()).isEqualTo("Promo2");
        assertThat(entity.getDescription()).isEqualTo("D2");
        assertThat(entity.getDiscountValue()).isEqualTo(7.5);
        assertThat(entity.getStartDate()).isEqualTo(LocalDate.of(2025,3,1));
        assertThat(entity.getEndDate()).isEqualTo(LocalDate.of(2025,3,31));
        // status y user quedan null por ignore
        assertThat(entity.getStatus()).isNull();
        assertThat(entity.getUser()).isNull();
    }

    @Test
    @DisplayName("updateEntityFromDTO actualiza campos sin modificar ID")
    void updateEntityFromDTO_updatesFields() {
        Promotion entity = new Promotion();
        entity.setPromotionId(7);
        entity.setPromotionName("Old");
        entity.setDescription("OldD");
        entity.setDiscountValue(2.0);
        entity.setStartDate(LocalDate.of(2025,1,1));
        entity.setEndDate(LocalDate.of(2025,1,10));

        PromotionDTO dto = new PromotionDTO();
        dto.setPromotionName("New");
        dto.setDescription("NewD");
        dto.setDiscountPercentage(BigDecimal.valueOf(9.0));
        dto.setStartDate(LocalDateTime.of(2025,1,5,0,0));
        dto.setEndDate(LocalDateTime.of(2025,1,20,0,0));

        mapper.updateEntityFromDTO(dto, entity);

        assertThat(entity.getPromotionId()).isEqualTo(7); // no cambia
        assertThat(entity.getPromotionName()).isEqualTo("New");
        assertThat(entity.getDescription()).isEqualTo("NewD");
        assertThat(entity.getDiscountValue()).isEqualTo(9.0);
        assertThat(entity.getStartDate()).isEqualTo(LocalDate.of(2025,1,5));
        assertThat(entity.getEndDate()).isEqualTo(LocalDate.of(2025,1,20));
    }
}
