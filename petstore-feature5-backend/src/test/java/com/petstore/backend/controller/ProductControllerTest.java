package com.petstore.backend.controller;

import com.petstore.backend.dto.CategoryDTO;
import com.petstore.backend.entity.Category;
import com.petstore.backend.entity.Product;
import com.petstore.backend.mapper.MapperFacade;
import com.petstore.backend.service.ProductService;
import com.petstore.backend.service.AuthService;
import com.petstore.backend.config.JwtAuthenticationFilter;
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

import java.util.List;
import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = ProductController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class
        }
)
@AutoConfigureMockMvc(addFilters = false)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    // Satisface la inyección en el controlador, aunque no se use en los métodos actuales
    @MockBean
    private MapperFacade mapperFacade;

    // Evita que Spring Security intente crear beans reales durante el slice test
    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private AuthService authService;

    @MockBean
    private CorsConfigurationSource corsConfigurationSource;

    private Product product(int id, String name, double price, int sku, Category category) {
        Product p = new Product();
        p.setProductId(id);
        p.setProductName(name);
        p.setBasePrice(price);
        p.setSku(sku);
        p.setCategory(category);
        return p;
    }

    private Category category(int id, String name) {
        Category c = new Category();
        c.setCategoryId(id);
        c.setCategoryName(name);
        return c;
    }

    @Test
    @DisplayName("GET /api/products devuelve 200 y lista de productos")
    void getAllProducts_returnsOkWithList() throws Exception {
        Category cat = category(1, "Perros");
        Product p1 = product(10, "Collar", 25.5, 1001, cat);
        Product p2 = product(11, "Correa", 30.0, 1002, cat);
        given(productService.findAll()).willReturn(List.of(p1, p2));

        mockMvc.perform(get("/api/products").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].productId").value(10))
                .andExpect(jsonPath("$[0].productName").value("Collar"))
                .andExpect(jsonPath("$[0].description").value("Collar"))
                .andExpect(jsonPath("$[0].price").value(25.5))
                .andExpect(jsonPath("$[0].category.categoryId").value(1))
                .andExpect(jsonPath("$[1].productId").value(11))
                .andExpect(jsonPath("$[1].productName").value("Correa"))
                .andExpect(jsonPath("$[1].description").value("Correa"))
                .andExpect(jsonPath("$[1].price").value(30.0));
    }

    @Test
    @DisplayName("GET /api/products/{id} devuelve 200 cuando existe")
    void getProductById_returnsOk_whenExists() throws Exception {
        Category cat = category(2, "Gatos");
        Product p = product(20, "Rascador", 99.99, 2001, cat);
        given(productService.findById(20)).willReturn(Optional.of(p));

        mockMvc.perform(get("/api/products/{id}", 20).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.productId").value(20))
                .andExpect(jsonPath("$.productName").value("Rascador"))
                .andExpect(jsonPath("$.description").value("Rascador"))
                .andExpect(jsonPath("$.price").value(99.99))
                .andExpect(jsonPath("$.category.categoryId").value(2))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("GET /api/products/{id} devuelve 404 cuando no existe")
    void getProductById_returnsNotFound_whenMissing() throws Exception {
        given(productService.findById(404)).willReturn(Optional.empty());

        mockMvc.perform(get("/api/products/{id}", 404).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/products/category/{categoryId} devuelve 200 con lista")
    void getProductsByCategory_returnsOkWithList() throws Exception {
        Category cat = category(3, "Aves");
        Product p = product(30, "Comedero Aves", 15.0, 3001, cat);
        given(productService.findByCategoryId(3)).willReturn(List.of(p));

        mockMvc.perform(get("/api/products/category/{categoryId}", 3).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productId").value(30))
                .andExpect(jsonPath("$[0].category.categoryId").value(3));
    }

    @Test
    @DisplayName("GET /api/products/search?name=... devuelve 200 con lista")
    void searchProducts_returnsOkWithList() throws Exception {
        Category cat = category(4, "Peces");
        Product p = product(40, "Pecera", 120.0, 4001, cat);
        given(productService.findByNameContaining("pec")).willReturn(List.of(p));

        mockMvc.perform(get("/api/products/search").param("name", "pec").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productId").value(40))
                .andExpect(jsonPath("$[0].productName").value("Pecera"));
    }

    @Test
    @DisplayName("GET /api/products/price-range devuelve 200 con lista")
    void getProductsByPriceRange_returnsOkWithList() throws Exception {
        Category cat = category(5, "Roedores");
        Product p = product(50, "Jaula", 200.0, 5001, cat);
        given(productService.findByPriceBetween(100.0, 250.0)).willReturn(List.of(p));

        mockMvc.perform(get("/api/products/price-range")
                        .param("minPrice", "100.0")
                        .param("maxPrice", "250.0")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productId").value(50))
                .andExpect(jsonPath("$[0].price").value(200.0));
    }
}
