package com.petstore.backend.service;

import com.petstore.backend.entity.Category;
import com.petstore.backend.repository.CategoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category category(int id, String name) {
        Category c = new Category();
        c.setCategoryId(id);
        c.setCategoryName(name);
        return c;
    }

    @Test
    @DisplayName("findAll retorna lista del repositorio")
    void findAll_returnsRepositoryList() {
        given(categoryRepository.findAll()).willReturn(List.of(category(1, "Perros"), category(2, "Gatos")));

        List<Category> result = categoryService.findAll();

        assertThat(result).hasSize(2);
        verify(categoryRepository).findAll();
    }

    @Test
    @DisplayName("findById retorna Optional cuando existe")
    void findById_returnsOptional_whenExists() {
        given(categoryRepository.findById(10)).willReturn(Optional.of(category(10, "Aves")));

        Optional<Category> result = categoryService.findById(10);

        assertThat(result).isPresent();
        assertThat(result.get().getCategoryName()).isEqualTo("Aves");
        verify(categoryRepository).findById(10);
    }

    @Test
    @DisplayName("save delega en repository.save")
    void save_delegatesToRepository() {
        Category in = category(0, "Peces");
        Category out = category(33, "Peces");
        given(categoryRepository.save(in)).willReturn(out);

        Category result = categoryService.save(in);

        assertThat(result.getCategoryId()).isEqualTo(33);
        verify(categoryRepository).save(in);
    }

    @Test
    @DisplayName("deleteById delega en repository.deleteById")
    void deleteById_delegatesToRepository() {
        categoryService.deleteById(5);
        verify(categoryRepository).deleteById(5);
    }

    @Test
    @DisplayName("existsById retorna el valor del repositorio")
    void existsById_returnsRepositoryValue() {
        given(categoryRepository.existsById(7)).willReturn(true);
        boolean exists = categoryService.existsById(7);
        assertThat(exists).isTrue();
        verify(categoryRepository).existsById(7);
    }
}
