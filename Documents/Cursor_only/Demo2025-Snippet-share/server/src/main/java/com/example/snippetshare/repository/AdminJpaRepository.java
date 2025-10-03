package com.example.snippetshare.repository;

import com.example.snippetshare.entity.AdminEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AdminJpaRepository extends JpaRepository<AdminEntity, UUID> {
    boolean existsByNameIgnoreCase(String name);
    Optional<AdminEntity> findByNameIgnoreCase(String name);
}


