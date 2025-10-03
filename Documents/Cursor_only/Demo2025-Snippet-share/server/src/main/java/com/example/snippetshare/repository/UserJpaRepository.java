package com.example.snippetshare.repository;

import com.example.snippetshare.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserJpaRepository extends JpaRepository<UserEntity, UUID> {
    Optional<UserEntity> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);
}
