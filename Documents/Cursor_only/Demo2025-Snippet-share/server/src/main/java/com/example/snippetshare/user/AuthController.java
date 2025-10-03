package com.example.snippetshare.user;

import com.example.snippetshare.entity.UserEntity;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserEntity register(@Valid @RequestBody AuthService.RegisterRequest request) {
        try {
            return authService.register(request);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, ex.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<UserEntity> login(@Valid @RequestBody AuthService.LoginRequest request) {
        return authService.login(request)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    @GetMapping("/users")
    public java.util.List<String> listUsers() {
        return authService.listUsers();
    }
}


