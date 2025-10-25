package com.petstore.backend.repository;

import com.petstore.backend.entity.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StatusRepository extends JpaRepository<Status, Integer> {
    
    // Buscar status por nombre
    Optional<Status> findByStatusName(String statusName);
    
    // Verificar si existe un status con el nombre
    boolean existsByStatusName(String statusName);
}
