package com.petstore.backend.service;

import com.petstore.backend.entity.Category;
import com.petstore.backend.entity.Product;
import com.petstore.backend.repository.CategoryRepository;
import com.petstore.backend.repository.ProductRepository;
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
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductService productService;

    private Category category(int id, String name) {
        Category c = new Category();
        c.setCategoryId(id);
        c.setCategoryName(name);
        return c;
    }

    private Product product(int id, String name, double price, Integer categoryId) {
        Product p = new Product();
        p.setProductId(id);
        p.setProductName(name);
        p.setBasePrice(price);
        if (categoryId != null) {
            p.setCategory(category(categoryId, "Cat" + categoryId));
        }
        return p;
    }

    @Test
    @DisplayName("findAll delega en repository.findAll")
    void findAll_delegates() {
        given(productRepository.findAll()).willReturn(List.of(product(1, "A", 10.0, 1)));
        List<Product> result = productService.findAll();
        assertThat(result).hasSize(1);
        verify(productRepository).findAll();
    }

    @Test
    @DisplayName("findByCategoryId devuelve lista del repositorio")
    void findByCategoryId_returnsList() {
        given(productRepository.findByCategoryCategoryId(3)).willReturn(List.of(product(2, "B", 20.0, 3)));
        List<Product> result = productService.findByCategoryId(3);
        assertThat(result).hasSize(1);
        verify(productRepository).findByCategoryCategoryId(3);
    }

    @Test
    @DisplayName("findById retorna Optional cuando existe")
    void findById_returnsOptional() {
        given(productRepository.findById(5)).willReturn(Optional.of(product(5, "C", 30.0, null)));
        Optional<Product> result = productService.findById(5);
        assertThat(result).isPresent();
        assertThat(result.get().getProductName()).isEqualTo("C");
        verify(productRepository).findById(5);
    }

    @Test
    @DisplayName("save delega en repository.save")
    void save_delegates() {
        Product in = product(0, "D", 40.0, 2);
        Product out = product(10, "D", 40.0, 2);
        given(productRepository.save(in)).willReturn(out);
        Product result = productService.save(in);
        assertThat(result.getProductId()).isEqualTo(10);
        verify(productRepository).save(in);
    }

    @Test
    @DisplayName("deleteById delega en repository.deleteById")
    void deleteById_delegates() {
        productService.deleteById(9);
        verify(productRepository).deleteById(9);
    }

    @Test
    @DisplayName("findByNameContaining delega en repository.findByProductNameContainingIgnoreCase")
    void findByNameContaining_delegates() {
        given(productRepository.findByProductNameContainingIgnoreCase("abc"))
                .willReturn(List.of(product(11, "abc-1", 50.0, null)));
        List<Product> result = productService.findByNameContaining("abc");
        assertThat(result).hasSize(1);
        verify(productRepository).findByProductNameContainingIgnoreCase("abc");
    }

    @Test
    @DisplayName("findByPriceBetween delega en repository.findByBasePriceBetween")
    void findByPriceBetween_delegates() {
        given(productRepository.findByBasePriceBetween(10.0, 20.0))
                .willReturn(List.of(product(12, "E", 15.0, null)));
        List<Product> result = productService.findByPriceBetween(10.0, 20.0);
        assertThat(result).hasSize(1);
        verify(productRepository).findByBasePriceBetween(10.0, 20.0);
    }

    @Test
    @DisplayName("existsById retorna el valor del repositorio")
    void existsById_returnsRepoValue() {
        given(productRepository.existsById(77)).willReturn(true);
        boolean exists = productService.existsById(77);
        assertThat(exists).isTrue();
        verify(productRepository).existsById(77);
    }

    @Test
    @DisplayName("count delega en repository.count")
    void count_delegates() {
        given(productRepository.count()).willReturn(42L);
        long count = productService.count();
        assertThat(count).isEqualTo(42L);
        verify(productRepository).count();
    }
}
