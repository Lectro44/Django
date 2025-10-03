package com.example.snippetshare.snippet;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.snippetshare.entity.SnippetEntity;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/snippets")
public class SnippetController {
    @GetMapping("/code/{shareCode}")
    public ResponseEntity<SnippetEntity> getByShareCode(@PathVariable("shareCode") String shareCode) {
        return service.getByShareCode(shareCode)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private static final Logger logger = LoggerFactory.getLogger(SnippetController.class);
    private final SnippetService service;

    public SnippetController(SnippetService service) {
        this.service = service;
    }

    @GetMapping
    public List<SnippetEntity> list() {
        return service.list();
    }

    @GetMapping("/guest")
    public List<SnippetEntity> listGuest(@RequestHeader(value = "X-Guest-Id", required = false) String guestId) {
        return service.listGuest(guestId);
    }

    @GetMapping("/me")
    public ResponseEntity<List<SnippetEntity>> listMe(@RequestHeader(value = "X-User-Name", required = false) String userName) {
        if (userName == null || userName.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        return ResponseEntity.ok(service.listForUserName(userName));
    }

    @GetMapping("/languages")
    public List<Language> languages() {
        return Arrays.asList(Language.values());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SnippetEntity> get(@PathVariable("id") UUID id) {
        return service.get(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SnippetEntity create(@RequestHeader(value = "X-User-Name", required = false) String userName,
                                @RequestHeader(value = "X-Guest-Id", required = false) String guestId,
                                @Valid @RequestBody SnippetService.CreateSnippetRequest request) {
        return service.create(request, userName, guestId);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") UUID id) {
        logger.info("Attempting to delete snippet with ID: {}", id);
        boolean removed = service.delete(id);
        if (removed) {
            logger.info("Successfully deleted snippet with ID: {}", id);
            return ResponseEntity.noContent().build();
        } else {
            logger.warn("Failed to delete snippet with ID: {} - snippet not found", id);
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/share")
    @ResponseStatus(HttpStatus.CREATED)
    public SnippetEntity share(@RequestHeader(value = "X-User-Name", required = false) String userName,
                              @Valid @RequestBody SnippetService.CreateSnippetRequest request) {
        return service.share(request, userName);
    }

    @GetMapping("/shared")
    public List<SnippetDto> listShared() {
        return service.listShared().stream()
                .map(SnippetDto::fromEntity)
                .toList();
    }

    @GetMapping("/shared/me")
    public ResponseEntity<List<SnippetDto>> listSharedForUser(@RequestHeader(value = "X-User-Name", required = false) String userName) {
        if (userName == null || userName.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        List<SnippetDto> sharedSnippets = service.listSharedWithUser(userName).stream()
                .map(SnippetDto::fromEntity)
                .toList();
        return ResponseEntity.ok(sharedSnippets);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SnippetEntity> update(@PathVariable("id") UUID id,
                                               @RequestHeader(value = "X-User-Name", required = false) String userName,
                                               @RequestHeader(value = "X-Guest-Id", required = false) String guestId,
                                               @Valid @RequestBody SnippetService.CreateSnippetRequest request) {
        return service.update(id, request, userName, guestId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Receiver can push their changes back to original if allowed
    @PutMapping("/{id}/push-to-original")
    public ResponseEntity<Void> pushToOriginal(@PathVariable("id") UUID receiverCopyId,
                                               @RequestHeader(value = "X-User-Name", required = false) String userName) {
        boolean ok = service.pushCopyEditsToOriginal(receiverCopyId, userName);
        return ok ? ResponseEntity.noContent().build() : ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    public record ShareToUsersRequest(List<String> userNames, Boolean canWrite) {}

    @PostMapping("/{id}/share-to")
    public ResponseEntity<Void> shareToUsers(@PathVariable("id") UUID id,
                                             @RequestHeader(value = "X-User-Name", required = false) String userName,
                                             @RequestBody ShareToUsersRequest request) {
        if (request == null || request.userNames() == null || request.userNames().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        boolean canWrite = request.canWrite() != null && request.canWrite();
        service.shareToRecipients(id, request.userNames(), canWrite, userName);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/generate-code")
    public ResponseEntity<Map<String, String>> generateShareCode(@PathVariable("id") UUID id) {
        Optional<SnippetEntity> snippetOpt = service.getSnippetById(id);
        if (snippetOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        SnippetEntity snippet = snippetOpt.get();
        String code = service.generateUniqueShareCode();
        snippet.setShareCode(code);
        service.saveSnippet(snippet);

        Map<String, String> response = new HashMap<>();
        response.put("code", code);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/guest/by-session")
    public ResponseEntity<Void> deleteGuestBySession(@RequestHeader(value = "X-Guest-Id", required = false) String guestId,
                                                     @org.springframework.web.bind.annotation.RequestParam(value = "guestId", required = false) String guestIdParam) {
        String gid = (guestId != null && !guestId.isBlank()) ? guestId : guestIdParam;
        if (gid == null || gid.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        service.deleteGuestBySession(gid);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/guest/cleanup")
    public ResponseEntity<Void> cleanupGuestBySession(@RequestHeader(value = "X-Guest-Id", required = false) String guestId,
                                                      @org.springframework.web.bind.annotation.RequestParam(value = "guestId", required = false) String guestIdParam) {
        String gid = (guestId != null && !guestId.isBlank()) ? guestId : guestIdParam;
        if (gid == null || gid.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        service.deleteGuestBySession(gid);
        return ResponseEntity.noContent().build();
    }
}


