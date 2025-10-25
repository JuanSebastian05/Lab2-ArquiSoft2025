package com.petstore.backend.controller;

import com.petstore.backend.config.JwtAuthenticationFilter;
import com.petstore.backend.dto.CategoryDTO;
import com.petstore.backend.dto.PromotionDTO;
import com.petstore.backend.service.AuthService;
import com.petstore.backend.service.PromotionService;
import com.petstore.backend.util.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.cors.CorsConfigurationSource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = PromotionController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class
        }
)
@AutoConfigureMockMvc(addFilters = false)
class PromotionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PromotionService promotionService;

    // Security-related beans mocked to avoid context initialization of real security
    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private AuthService authService;

    @MockBean
    private CorsConfigurationSource corsConfigurationSource;

    private PromotionDTO promo(int id, String name, double discount, Integer categoryId, String categoryName) {
        PromotionDTO dto = new PromotionDTO();
        dto.setPromotionId(id);
        dto.setPromotionName(name);
        dto.setDescription(name + " desc");
        dto.setDiscountPercentage(BigDecimal.valueOf(discount));
        dto.setStartDate(LocalDateTime.now().minusDays(1));
        dto.setEndDate(LocalDateTime.now().plusDays(1));
        dto.setStatus("ACTIVE");
        if (categoryId != null) {
            CategoryDTO c = new CategoryDTO();
            c.setCategoryId(categoryId);
            c.setCategoryName(categoryName);
            dto.setCategory(c);
        }
        return dto;
    }

    @Test
    @DisplayName("GET /api/promotions devuelve 200 con lista activa")
    void getAllActivePromotions_returnsOkWithList() throws Exception {
        given(promotionService.getAllActivePromotions()).willReturn(List.of(
                promo(1, "Promo Activa", 10.0, 5, "Accesorios")
        ));

        mockMvc.perform(get("/api/promotions").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].promotionId").value(1))
                .andExpect(jsonPath("$[0].promotionName").value("Promo Activa"))
                .andExpect(jsonPath("$[0].discountPercentage").value(10.0))
                .andExpect(jsonPath("$[0].category.categoryId").value(5));
    }

    @Test
    @DisplayName("GET /api/promotions/all devuelve 200 con lista completa")
    void getAllPromotions_returnsOkWithList() throws Exception {
        given(promotionService.getAllPromotions()).willReturn(List.of(
                promo(2, "Promo 1", 5.0, null, null),
                promo(3, "Promo 2", 15.0, 7, "Higiene")
        ));

        mockMvc.perform(get("/api/promotions/all").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].promotionId").value(2))
                .andExpect(jsonPath("$[1].promotionId").value(3))
                .andExpect(jsonPath("$[1].category.categoryId").value(7));
    }

    @Test
    @DisplayName("GET /api/promotions/category/{id} devuelve 200 por categoría")
    void getPromotionsByCategory_returnsOkWithList() throws Exception {
        given(promotionService.getPromotionsByCategory(9)).willReturn(List.of(
                promo(4, "Promo Cat", 12.5, 9, "Gatos")
        ));

        mockMvc.perform(get("/api/promotions/category/{categoryId}", 9).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].promotionId").value(4))
                .andExpect(jsonPath("$[0].category.categoryId").value(9));
    }

    @Test
    @DisplayName("GET /api/promotions/valid devuelve 200 con lista vigente")
    void getValidPromotions_returnsOkWithList() throws Exception {
        given(promotionService.getValidPromotions()).willReturn(List.of(
                promo(5, "Promo Vigente", 20.0, 1, "Perros")
        ));

        mockMvc.perform(get("/api/promotions/valid").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].promotionId").value(5))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));
    }

    @Test
    @DisplayName("GET /api/promotions/status devuelve 200 con mensaje")
    void getStatus_returnsOk() throws Exception {
        mockMvc.perform(get("/api/promotions/status").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("Promotion service is running"));
    }
}
