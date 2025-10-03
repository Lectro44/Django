package com.example.snippetshare.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.snippetshare.entity.SnippetEntity;
import com.example.snippetshare.entity.UserEntity;
import com.example.snippetshare.snippet.Language;

@Repository
public interface SnippetJpaRepository extends JpaRepository<SnippetEntity, UUID> {
    SnippetEntity findByShareCode(String shareCode);
    @Query("SELECT s FROM SnippetEntity s JOIN FETCH s.user WHERE s.id IN :ids")
    List<SnippetEntity> findAllByIdInWithUser(@Param("ids") List<UUID> ids);
    
    List<SnippetEntity> findAllByOrderByCreatedAtDesc();
    
    Page<SnippetEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    List<SnippetEntity> findByLanguageOrderByCreatedAtDesc(Language language);
    
    List<SnippetEntity> findByUserIdOrderByCreatedAtDesc(UUID userId);

    List<SnippetEntity> findByUserIsNullOrderByCreatedAtDesc();
    List<SnippetEntity> findByUserIsNullAndGuestSessionIdOrderByCreatedAtDesc(String guestSessionId);
    
    List<SnippetEntity> findByIsSharedTrueOrderByCreatedAtDesc();
    
    List<SnippetEntity> findBySharedByUserIdOrderByCreatedAtDesc(UUID sharedByUserId);
    List<SnippetEntity> findByUserIdAndOriginalSnippetIdIsNotNullOrderByCreatedAtDesc(UUID userId);
    @Query("SELECT s FROM SnippetEntity s LEFT JOIN FETCH s.sharedByUser LEFT JOIN FETCH s.user WHERE s.user.id = :userId AND s.originalSnippetId IS NOT NULL ORDER BY s.createdAt DESC")
    List<SnippetEntity> findReceiverCopiesWithUsers(@Param("userId") UUID userId);
    
    List<SnippetEntity> findByUser(UserEntity user);
    long countByUser(UserEntity user);
    List<SnippetEntity> findByTitleContainingIgnoreCase(String title);
    void deleteByUser(UserEntity user);
    void deleteByUserIsNullAndGuestSessionId(String guestSessionId);
    
    @Query("SELECT s FROM SnippetEntity s WHERE " +
           "LOWER(s.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(s.code) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "ORDER BY s.createdAt DESC")
    List<SnippetEntity> searchSnippets(@Param("query") String query);

    @Query("SELECT s FROM SnippetEntity s LEFT JOIN FETCH s.user ORDER BY s.createdAt DESC")
    List<SnippetEntity> findAllWithUser();

    @Query("SELECT s FROM SnippetEntity s LEFT JOIN FETCH s.user WHERE LOWER(s.title) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(s.code) LIKE LOWER(CONCAT('%', :query, '%')) ORDER BY s.createdAt DESC")
    List<SnippetEntity> searchSnippetsWithUser(@Param("query") String query);

    @Query("SELECT s FROM SnippetEntity s LEFT JOIN FETCH s.user LEFT JOIN FETCH s.sharedByUser WHERE s.user.id = :userId ORDER BY s.createdAt DESC")
    List<SnippetEntity> findByUserIdWithUser(@Param("userId") UUID userId);
}
