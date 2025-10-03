package com.example.snippetshare.snippet;

import com.example.snippetshare.entity.SnippetEntity;

import java.time.Instant;
import java.util.UUID;

public record SnippetDto(
        UUID id,
        String title,
        String code,
        Language language,
        Instant createdAt,
        String userName,
        Boolean isShared,
        String sharedBy,
        String lastEditedByName,
        Instant lastEditedAt,
        Boolean sharedCanEdit
) {
    public static SnippetDto fromEntity(SnippetEntity entity) {
        String userName = entity.getUser() != null ? entity.getUser().getName() : null;
        String sharedBy = entity.getSharedByUser() != null ? entity.getSharedByUser().getName() : null;
        
        return new SnippetDto(
                entity.getId(),
                entity.getTitle(),
                entity.getCode(),
                entity.getLanguage(),
                entity.getCreatedAt(),
                userName,
                entity.getIsShared(),
                sharedBy,
                entity.getLastEditedByName(),
                entity.getLastEditedAt(),
                entity.isSharedCanEdit()
        );
    }

    public static SnippetDto fromEntity(SnippetEntity entity, String userName) {
        String sharedBy = entity.getSharedByUser() != null ? entity.getSharedByUser().getName() : null;
        
        return new SnippetDto(
                entity.getId(),
                entity.getTitle(),
                entity.getCode(),
                entity.getLanguage(),
                entity.getCreatedAt(),
                userName,
                entity.getIsShared(),
                sharedBy,
                entity.getLastEditedByName(),
                entity.getLastEditedAt(),
                entity.isSharedCanEdit()
        );
    }
}
