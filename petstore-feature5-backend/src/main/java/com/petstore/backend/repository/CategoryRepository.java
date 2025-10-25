package com.petstore.backend.repository;

import com.petstore.backend.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {
    
    // Buscar categoría por nombre
    Optional<Category> findByCategoryName(String categoryName);
    
    // Verificar si existe una categoría con el nombre
    boolean existsByCategoryName(String categoryName);
}
