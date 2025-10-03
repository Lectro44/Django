package com.example.snippetshare.snippet;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.snippetshare.entity.SnippetEntity;
import com.example.snippetshare.entity.UserEntity;
import com.example.snippetshare.repository.SnippetJpaRepository;
import com.example.snippetshare.repository.SnippetRecipientRepository;
import com.example.snippetshare.repository.UserJpaRepository;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Service
@Transactional
public class SnippetService {

    private final SnippetJpaRepository repository;
    private final UserJpaRepository userRepository;
    private final SnippetRecipientRepository recipientRepository;

    public SnippetService(SnippetJpaRepository repository, UserJpaRepository userRepository, SnippetRecipientRepository recipientRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.recipientRepository = recipientRepository;
    }

    public SnippetEntity create(@Valid CreateSnippetRequest request, String userNameOrNull, String guestIdOrNull) {
        SnippetEntity snippet = new SnippetEntity(request.title(), request.code(), request.language());
        if (userNameOrNull != null && !userNameOrNull.isBlank()) {
            userRepository.findByNameIgnoreCase(userNameOrNull)
                    .ifPresent(snippet::setUser);
        } else {
            // Guest mode: generate unique 6-digit code
            String code;
            do {
                code = String.format("%06d", (int)(Math.random() * 1000000));
            } while (repository.findByShareCode(code) != null);
            snippet.setShareCode(code);
            if (guestIdOrNull != null && !guestIdOrNull.isBlank()) {
                snippet.setGuestSessionId(guestIdOrNull);
            }
        }
        return repository.save(snippet);
    }

    public Optional<SnippetEntity> getByShareCode(String shareCode) {
        SnippetEntity snippet = repository.findByShareCode(shareCode);
        return Optional.ofNullable(snippet);
    }

    public Optional<SnippetEntity> get(UUID id) {
        return repository.findById(id);
    }

    public List<SnippetEntity> list() {
        return repository.findAllByOrderByCreatedAtDesc();
    }

    public List<SnippetEntity> listGuest(String guestIdOrNull) {
        if (guestIdOrNull != null && !guestIdOrNull.isBlank()) {
            return repository.findByUserIsNullAndGuestSessionIdOrderByCreatedAtDesc(guestIdOrNull);
        }
        return repository.findByUserIsNullOrderByCreatedAtDesc();
    }

    public List<SnippetEntity> listForUserName(String name) {
        return userRepository.findByNameIgnoreCase(name)
                .map(UserEntity::getId)
                .map(repository::findByUserIdOrderByCreatedAtDesc)
                .orElseGet(List::of);
    }

    public boolean delete(UUID id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        }
        return false;
    }

    public SnippetEntity share(@Valid CreateSnippetRequest request, String userNameOrNull) {
        SnippetEntity snippet = new SnippetEntity(request.title(), request.code(), request.language());
        snippet.setIsShared(true);
        
        if (userNameOrNull != null && !userNameOrNull.isBlank()) {
            userRepository.findByNameIgnoreCase(userNameOrNull)
                    .ifPresent(snippet::setSharedByUser);
        }
        return repository.save(snippet);
    }

    public List<SnippetEntity> listShared() {
        return repository.findByIsSharedTrueOrderByCreatedAtDesc();
    }

    public List<SnippetEntity> listSharedForUser(String userName) {
        return userRepository.findByNameIgnoreCase(userName)
                .map(UserEntity::getId)
                .map(repository::findBySharedByUserIdOrderByCreatedAtDesc)
                .orElseGet(List::of);
    }

    public Optional<SnippetEntity> update(UUID id, @Valid CreateSnippetRequest request, String userNameOrNull, String guestIdOrNull) {
        return repository.findById(id)
                .map(snippet -> {
                    // Permission check: owners, sharers, write-enabled recipients, or matching guest session (for guest snippets)
                    boolean allowed = false;
                    if (userNameOrNull != null && !userNameOrNull.isBlank()) {
                        var userOpt = userRepository.findByNameIgnoreCase(userNameOrNull);
                        if (userOpt.isPresent()) {
                            var user = userOpt.get();
                            if ((snippet.getUser() != null && user.getId().equals(snippet.getUser().getId())) ||
                                (snippet.getSharedByUser() != null && user.getId().equals(snippet.getSharedByUser().getId()))) {
                                allowed = true;
                            } else {
                                var recips = recipientRepository.findBySnippet_Id(snippet.getId());
                                allowed = recips.stream().anyMatch(r -> r.getUser().getId().equals(user.getId()) && r.isCanWrite());
                            }
                        }
                    } else if (guestIdOrNull != null && !guestIdOrNull.isBlank()) {
                        // Guests can edit only their own guest snippets
                        allowed = (snippet.getUser() == null) && guestIdOrNull.equals(snippet.getGuestSessionId());
                    }
                    if (!allowed) {
                        return null; // will translate to Optional.empty()
                    }
                    snippet.setTitle(request.title());
                    snippet.setCode(request.code());
                    snippet.setLanguage(request.language());
                    // Preserve existing share code; if absent, generate for guests
                    if (snippet.getShareCode() == null || snippet.getShareCode().isBlank()) {
                        String code;
                        do {
                            code = String.format("%06d", (int)(Math.random() * 1000000));
                        } while (repository.findByShareCode(code) != null);
                        snippet.setShareCode(code);
                    }
                    // Audit who edited last
                    if (userNameOrNull != null && !userNameOrNull.isBlank()) {
                        snippet.setLastEditedByName(userNameOrNull);
                    } else if (guestIdOrNull != null && !guestIdOrNull.isBlank()) {
                        snippet.setLastEditedByName("guest:" + guestIdOrNull);
                    }
                    snippet.setLastEditedAt(java.time.Instant.now());
                    return repository.save(snippet);
                })
                .map(java.util.Optional::ofNullable)
                .orElseGet(java.util.Optional::empty);
    }

    public void shareToRecipients(UUID snippetId, List<String> recipientUserNames, boolean canWrite, String currentUserName) {
        var snippetOpt = repository.findById(snippetId);
        if (snippetOpt.isEmpty()) return;
        var snippet = snippetOpt.get();
        
        // Find the current user who is doing the sharing
        final UserEntity currentUser;
        if (currentUserName != null && !currentUserName.isBlank()) {
            currentUser = userRepository.findByNameIgnoreCase(currentUserName).orElse(null);
        } else {
            currentUser = null;
        }
        
        // mark as shared and set sender to current user (who is doing the sharing)
        snippet.setIsShared(true);
        if (currentUser != null) {
            snippet.setSharedByUser(currentUser);
        } else if (snippet.getSharedByUser() == null && snippet.getUser() != null) {
            snippet.setSharedByUser(snippet.getUser());
        }
        repository.save(snippet);

        for (String name : recipientUserNames) {
            userRepository.findByNameIgnoreCase(name).ifPresent(user -> {
                // Create receiver-owned copy
                SnippetEntity copy = new SnippetEntity(snippet.getTitle(), snippet.getCode(), snippet.getLanguage());
                copy.setUser(user);
                copy.setOriginalSnippetId(snippet.getId());
                copy.setSharedCanEdit(canWrite);
                copy.setIsShared(true);
                // Set the current user as the one who shared this copy
                if (currentUser != null) {
                    copy.setSharedByUser(currentUser);
                } else if (snippet.getSharedByUser() != null) {
                    copy.setSharedByUser(snippet.getSharedByUser());
                } else if (snippet.getUser() != null) {
                    copy.setSharedByUser(snippet.getUser());
                }
                repository.save(copy);
            });
        }
    }

    public List<SnippetEntity> listSharedWithUser(String userName) {
        return userRepository.findByNameIgnoreCase(userName)
                .map(user -> {
                    // return the receiver-owned copies
                    return repository.findReceiverCopiesWithUsers(user.getId());
                })
                .orElseGet(List::of);
    }

    public Optional<SnippetEntity> getSnippetById(UUID id) {
        return repository.findById(id);
    }

    public String generateUniqueShareCode() {
        String code;
        do {
            code = String.format("%06d", (int) (Math.random() * 1000000));
        } while (repository.findByShareCode(code) != null);
        return code;
    }

    public SnippetEntity saveSnippet(SnippetEntity snippet) {
        return repository.save(snippet);
    }

    public void deleteGuestBySession(String guestId) {
        repository.deleteByUserIsNullAndGuestSessionId(guestId);
    }

    public boolean pushCopyEditsToOriginal(UUID receiverCopyId, String userName) {
        if (userName == null || userName.isBlank()) return false;
        var userOpt = userRepository.findByNameIgnoreCase(userName);
        if (userOpt.isEmpty()) return false;
        var copyOpt = repository.findById(receiverCopyId);
        if (copyOpt.isEmpty()) return false;
        var copy = copyOpt.get();
        // Ensure this is a receiver-owned copy and the caller owns it
        if (copy.getOriginalSnippetId() == null || copy.getUser() == null || !copy.getUser().getId().equals(userOpt.get().getId())) return false;
        if (!copy.isSharedCanEdit()) return false;
        var originalOpt = repository.findById(copy.getOriginalSnippetId());
        if (originalOpt.isEmpty()) return false;
        var original = originalOpt.get();
        original.setTitle(copy.getTitle());
        original.setCode(copy.getCode());
        original.setLanguage(copy.getLanguage());
        original.setLastEditedByName(userName);
        original.setLastEditedAt(java.time.Instant.now());
        repository.save(original);
        return true;
    }

    public record CreateSnippetRequest(
            @NotBlank String title,
            @NotBlank String code,
            @NotNull Language language
    ) {}
}


