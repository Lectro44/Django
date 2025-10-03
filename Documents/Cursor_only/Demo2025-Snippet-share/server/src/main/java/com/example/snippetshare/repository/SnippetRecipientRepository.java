package com.example.snippetshare.repository;

import com.example.snippetshare.entity.SnippetRecipient;
import com.example.snippetshare.entity.SnippetRecipientId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SnippetRecipientRepository extends JpaRepository<SnippetRecipient, SnippetRecipientId> {
    List<SnippetRecipient> findByUser_Id(UUID userId);
    List<SnippetRecipient> findBySnippet_Id(UUID snippetId);
}


