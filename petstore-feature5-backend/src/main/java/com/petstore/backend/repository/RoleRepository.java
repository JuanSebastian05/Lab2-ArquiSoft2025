package com.petstore.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.petstore.backend.entity.Role;

import graphql.com.google.common.base.Optional;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByRoleName(String roleName);
}
