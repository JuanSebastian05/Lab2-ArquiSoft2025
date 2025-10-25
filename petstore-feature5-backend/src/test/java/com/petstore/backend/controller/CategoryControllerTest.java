package com.petstore.backend.controller;

import com.petstore.backend.config.JwtAuthenticationFilter;
import com.petstore.backend.entity.Category;
import com.petstore.backend.service.AuthService;
import com.petstore.backend.service.CategoryService;
import com.petstore.backend.util.JwtUtil;
import org.springframework.web.cors.CorsConfigurationSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    controllers = CategoryController.class,
    excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        SecurityFilterAutoConfiguration.class
    }
)
@AutoConfigureMockMvc(addFilters = false) // desactiva filtros de seguridad para pruebas de controlador puras
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CategoryService categoryService;

    // Mockeamos componentes de seguridad para que el contexto cargue en @WebMvcTest sin intentar instanciar filtros reales
    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private AuthService authService;

    @MockBean
    private CorsConfigurationSource corsConfigurationSource;

    @Test
    @DisplayName("GET /api/categories devuelve 200 y lista de categorías mapeadas a DTO")
    void getAllCategories_returnsOkWithDtoList() throws Exception {
        // Arrange: mockear el servicio para devolver entidades Category
        Category cat1 = new Category();
        cat1.setCategoryId(1);
        cat1.setCategoryName("Perros");
        cat1.setDescription("Productos para perros");

        Category cat2 = new Category();
        cat2.setCategoryId(2);
        cat2.setCategoryName("Gatos");
        cat2.setDescription("Productos para gatos");
        given(categoryService.findAll()).willReturn(List.of(cat1, cat2));

        // Act & Assert
        mockMvc.perform(get("/api/categories")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].categoryId").value(1))
                .andExpect(jsonPath("$[0].categoryName").value("Perros"))
                .andExpect(jsonPath("$[0].description").value("Productos para perros"))
                .andExpect(jsonPath("$[1].categoryId").value(2))
                .andExpect(jsonPath("$[1].categoryName").value("Gatos"))
                .andExpect(jsonPath("$[1].description").value("Productos para gatos"));
    }

    @Test
    @DisplayName("GET /api/categories/{id} devuelve 200 cuando existe")
    void getCategoryById_returnsOk_whenExists() throws Exception {
        // Arrange
        Category cat = new Category();
        cat.setCategoryId(10);
        cat.setCategoryName("Aves");
        cat.setDescription("Productos para aves");

        given(categoryService.findById(10)).willReturn(Optional.of(cat));

        // Act & Assert
        mockMvc.perform(get("/api/categories/{id}", 10)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.categoryId").value(10))
                .andExpect(jsonPath("$.categoryName").value("Aves"))
                .andExpect(jsonPath("$.description").value("Productos para aves"));
    }

    @Test
    @DisplayName("GET /api/categories/{id} devuelve 404 cuando no existe")
    void getCategoryById_returnsNotFound_whenMissing() throws Exception {
        // Arrange
        given(categoryService.findById(999)).willReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/categories/{id}", 999)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/categories devuelve 201 y el recurso creado")
    void createCategory_returnsCreated_whenValid() throws Exception {
    // Arrange: cuerpo de solicitud
    String requestJson = "{\n" +
        "  \"categoryName\": \"Reptiles\",\n" +
        "  \"description\": \"Productos para reptiles\"\n" +
        "}";

    // El servicio recibe una entidad sin id y retorna la entidad persistida con id
    Category toSave = new Category();
    toSave.setCategoryName("Reptiles");
    toSave.setDescription("Productos para reptiles");

    Category saved = new Category();
    saved.setCategoryId(21);
    saved.setCategoryName("Reptiles");
    saved.setDescription("Productos para reptiles");

    given(categoryService.save(org.mockito.ArgumentMatchers.any(Category.class)))
        .willReturn(saved);

    // Act & Assert
    mockMvc.perform(post("/api/categories")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestJson)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isCreated())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.categoryId").value(21))
        .andExpect(jsonPath("$.categoryName").value("Reptiles"))
        .andExpect(jsonPath("$.description").value("Productos para reptiles"));
    }

    @Test
    @DisplayName("PUT /api/categories/{id} devuelve 200 cuando existe y actualiza")
    void updateCategory_returnsOk_whenExists() throws Exception {
    // Arrange: existente
    Category existing = new Category();
    existing.setCategoryId(30);
    existing.setCategoryName("Viejo");
    existing.setDescription("Desc vieja");

    given(categoryService.findById(30)).willReturn(Optional.of(existing));

    Category updated = new Category();
    updated.setCategoryId(30);
    updated.setCategoryName("Nuevo");
    updated.setDescription("Desc nueva");
    given(categoryService.save(org.mockito.ArgumentMatchers.any(Category.class)))
        .willReturn(updated);

    String requestJson = "{\n" +
        "  \"categoryName\": \"Nuevo\",\n" +
        "  \"description\": \"Desc nueva\"\n" +
        "}";

    // Act & Assert
    mockMvc.perform(put("/api/categories/{id}", 30)
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestJson)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.categoryId").value(30))
        .andExpect(jsonPath("$.categoryName").value("Nuevo"))
        .andExpect(jsonPath("$.description").value("Desc nueva"));
    }

    @Test
    @DisplayName("PUT /api/categories/{id} devuelve 404 cuando no existe")
    void updateCategory_returnsNotFound_whenMissing() throws Exception {
    given(categoryService.findById(404)).willReturn(Optional.empty());

    String requestJson = "{\n" +
        "  \"categoryName\": \"Nuevo\",\n" +
        "  \"description\": \"Desc nueva\"\n" +
        "}";

    mockMvc.perform(put("/api/categories/{id}", 404)
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestJson)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/categories/{id} devuelve 204 cuando existe")
    void deleteCategory_returnsNoContent_whenExists() throws Exception {
    given(categoryService.existsById(77)).willReturn(true);

    mockMvc.perform(delete("/api/categories/{id}", 77))
        .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/categories/{id} devuelve 404 cuando no existe")
    void deleteCategory_returnsNotFound_whenMissing() throws Exception {
    given(categoryService.existsById(78)).willReturn(false);

    mockMvc.perform(delete("/api/categories/{id}", 78))
        .andExpect(status().isNotFound());
    }
}
