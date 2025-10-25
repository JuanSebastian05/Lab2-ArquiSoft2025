package com.petstore.backend.service;

import com.petstore.backend.dto.PromotionDTO;
import com.petstore.backend.entity.Category;
import com.petstore.backend.entity.Promotion;
import com.petstore.backend.entity.Status;
import com.petstore.backend.entity.User;
import com.petstore.backend.repository.CategoryRepository;
import com.petstore.backend.repository.PromotionRepository;
import com.petstore.backend.repository.StatusRepository;
import com.petstore.backend.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PromotionServiceTest {

    @Mock private PromotionRepository promotionRepository;
    @Mock private StatusRepository statusRepository;
    @Mock private UserRepository userRepository;
    @Mock private CategoryRepository categoryRepository;

    @InjectMocks
    private PromotionService promotionService;

    private Status status(int id, String name) {
        Status s = new Status();
        s.setStatusId(id);
        s.setStatusName(name);
        return s;
    }

    private Category category(int id, String name, String description) {
        Category c = new Category();
        c.setCategoryId(id);
        c.setCategoryName(name);
        c.setDescription(description);
        return c;
    }

    private User user(int id, String name, String email) {
        User u = new User();
        u.setUserId(id);
        u.setUserName(name);
        u.setEmail(email);
        u.setPassword("x");
        return u;
    }

    private Promotion promo(Integer id, String name, String desc, LocalDate start, LocalDate end,
                             double discount, Status status, User user, Category category) {
        Promotion p = new Promotion();
        p.setPromotionId(id);
        p.setPromotionName(name);
        p.setDescription(desc);
        p.setStartDate(start);
        p.setEndDate(end);
        p.setDiscountValue(discount);
        p.setStatus(status);
        p.setUser(user);
        p.setCategory(category);
        return p;
    }

    @Test
    @DisplayName("getAllActivePromotions filtra por fecha vigente y mapea a DTO")
    void getAllActivePromotions_filtersAndMaps() {
        LocalDate today = LocalDate.now();
        Status active = status(1, "ACTIVE");
        Category cat = category(10, "Perros", "Accesorios");

        Promotion valid = promo(1, "Promo OK", "desc", today.minusDays(1), today.plusDays(1), 10.0, active, null, cat);
        Promotion future = promo(2, "Promo FUT", "desc", today.plusDays(1), today.plusDays(2), 15.0, active, null, cat);
        Promotion past = promo(3, "Promo PAST", "desc", today.minusDays(3), today.minusDays(1), 5.0, active, null, cat);
        given(promotionRepository.findActivePromotions()).willReturn(List.of(valid, future, past));

        List<PromotionDTO> result = promotionService.getAllActivePromotions();

        assertThat(result).hasSize(1);
        PromotionDTO dto = result.get(0);
        assertThat(dto.getPromotionId()).isEqualTo(1);
        assertThat(dto.getPromotionName()).isEqualTo("Promo OK");
        assertThat(dto.getDiscountPercentage()).isEqualByComparingTo(BigDecimal.valueOf(10.0));
        assertThat(dto.getStatus()).isEqualTo("ACTIVE");
        assertThat(dto.getCategory().getCategoryId()).isEqualTo(10);
        assertThat(dto.getStartDate()).isEqualTo(valid.getStartDate().atStartOfDay());
        assertThat(dto.getEndDate()).isEqualTo(valid.getEndDate().atTime(23,59,59));
        verify(promotionRepository).findActivePromotions();
    }

    @Test
    @DisplayName("getAllPromotions mapea todas las promociones a DTO")
    void getAllPromotions_mapsAll() {
        LocalDate today = LocalDate.now();
        given(promotionRepository.findAll()).willReturn(List.of(
                promo(5, "P1", "d1", today, today.plusDays(1), 7.5, status(1, "ACTIVE"), null, null),
                promo(6, "P2", "d2", today, today.plusDays(2), 12.0, status(2, "EXPIRED"), null, null)
        ));

        List<PromotionDTO> result = promotionService.getAllPromotions();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getPromotionId()).isEqualTo(5);
        verify(promotionRepository).findAll();
    }

    @Test
    @DisplayName("getPromotionsByCategory usa repositorio y mapea a DTO")
    void getPromotionsByCategory_maps() {
        LocalDate today = LocalDate.now();
        Category cat = category(3, "Gatos", null);
        given(promotionRepository.findByCategoryCategoryId(3)).willReturn(List.of(
                promo(7, "PX", "dx", today, today.plusDays(1), 9.0, status(1, "ACTIVE"), null, cat)
        ));

        List<PromotionDTO> result = promotionService.getPromotionsByCategory(3);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCategory().getCategoryId()).isEqualTo(3);
        verify(promotionRepository).findByCategoryCategoryId(3);
    }

    @Test
    @DisplayName("getValidPromotions delega en repositorio (fecha actual) y mapea a DTO")
    void getValidPromotions_delegates() {
        LocalDate today = LocalDate.now();
        given(promotionRepository.findValidPromotions(any(LocalDate.class)))
                .willReturn(List.of(promo(9, "PV", "d", today.minusDays(1), today.plusDays(1), 8.0, status(1, "ACTIVE"), null, null)));

        List<PromotionDTO> result = promotionService.getValidPromotions();
        assertThat(result).hasSize(1);
        verify(promotionRepository).findValidPromotions(any(LocalDate.class));
    }

    @Test
    @DisplayName("createPromotion construye entidad con relaciones y guarda")
    void createPromotion_buildsAndSaves() {
        LocalDate today = LocalDate.now();
        Status st = status(1, "ACTIVE");
        User us = user(2, "Juan", "j@x.com");
        Category cat = category(3, "Aves", "desc");

        given(statusRepository.findById(1)).willReturn(Optional.of(st));
        given(userRepository.findById(2)).willReturn(Optional.of(us));
        given(categoryRepository.findById(3)).willReturn(Optional.of(cat));
        given(promotionRepository.save(any(Promotion.class))).willAnswer(inv -> {
            Promotion p = inv.getArgument(0);
            p.setPromotionId(99);
            return p;
        });

        Promotion saved = promotionService.createPromotion("New", "Desc", today, today.plusDays(2), 11.0, 1, 2, 3);

        assertThat(saved.getPromotionId()).isEqualTo(99);
        assertThat(saved.getPromotionName()).isEqualTo("New");
        assertThat(saved.getDiscountValue()).isEqualTo(11.0);
        assertThat(saved.getStatus()).isSameAs(st);
        assertThat(saved.getUser()).isSameAs(us);
        assertThat(saved.getCategory()).isSameAs(cat);
        verify(promotionRepository).save(any(Promotion.class));
    }

    @Test
    @DisplayName("updatePromotion actualiza campos y relaciones cuando existe")
    void updatePromotion_updatesWhenFound() {
        LocalDate today = LocalDate.now();
        Promotion existing = promo(10, "Old", "Od", today.minusDays(5), today.minusDays(1), 5.0, status(1, "ACTIVE"), null, null);
        given(promotionRepository.findById(10)).willReturn(Optional.of(existing));

        Status st2 = status(2, "EXPIRED");
        User us2 = user(4, "Ana", "a@x.com");
        Category cat2 = category(6, "Peces", "d");
        given(statusRepository.findById(2)).willReturn(Optional.of(st2));
        given(userRepository.findById(4)).willReturn(Optional.of(us2));
        given(categoryRepository.findById(6)).willReturn(Optional.of(cat2));
        given(promotionRepository.save(any(Promotion.class))).willAnswer(inv -> inv.getArgument(0));

        Promotion updated = promotionService.updatePromotion(10, "New", "Nd", today, today.plusDays(3), 12.5, 2, 4, 6);

        assertThat(updated.getPromotionName()).isEqualTo("New");
        assertThat(updated.getDescription()).isEqualTo("Nd");
        assertThat(updated.getStartDate()).isEqualTo(today);
        assertThat(updated.getEndDate()).isEqualTo(today.plusDays(3));
        assertThat(updated.getDiscountValue()).isEqualTo(12.5);
        assertThat(updated.getStatus()).isSameAs(st2);
        assertThat(updated.getUser()).isSameAs(us2);
        assertThat(updated.getCategory()).isSameAs(cat2);
    }

    @Test
    @DisplayName("updatePromotion retorna null cuando no existe")
    void updatePromotion_returnsNullWhenMissing() {
        given(promotionRepository.findById(anyInt())).willReturn(Optional.empty());
        Promotion res = promotionService.updatePromotion(999, null, null, null, null, null, null, null, null);
        assertThat(res).isNull();
    }

    @Test
    @DisplayName("deletePromotion retorna true cuando existe y elimina; false cuando no existe")
    void deletePromotion_behaviour() {
        given(promotionRepository.existsById(5)).willReturn(true);
        boolean ok = promotionService.deletePromotion(5);
        assertThat(ok).isTrue();
        verify(promotionRepository).deleteById(5);

        given(promotionRepository.existsById(6)).willReturn(false);
        boolean ko = promotionService.deletePromotion(6);
        assertThat(ko).isFalse();
        verify(promotionRepository, never()).deleteById(6);
    }

    @Test
    @DisplayName("getAllActivePromotionsEntities filtra por fecha vigente")
    void getAllActivePromotionsEntities_filters() {
        LocalDate today = LocalDate.now();
        Status active = status(1, "ACTIVE");
        Promotion valid = promo(1, "V", "d", today.minusDays(1), today.plusDays(1), 10.0, active, null, null);
        Promotion future = promo(2, "F", "d", today.plusDays(1), today.plusDays(2), 10.0, active, null, null);
        given(promotionRepository.findActivePromotions()).willReturn(List.of(valid, future));

        List<Promotion> entities = promotionService.getAllActivePromotionsEntities();
        assertThat(entities).extracting(Promotion::getPromotionId).containsExactly(1);
    }
}
