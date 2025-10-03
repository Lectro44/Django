package com.example.snippetshare.user;

import com.example.snippetshare.entity.UserEntity;
import com.example.snippetshare.repository.UserJpaRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AuthService {
    private final UserJpaRepository userRepository;
    private final JdbcTemplate jdbcTemplate;

    public AuthService(UserJpaRepository userRepository, JdbcTemplate jdbcTemplate) {
        this.userRepository = userRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    public UserEntity register(@Valid RegisterRequest request) {
        if (userRepository.existsByNameIgnoreCase(request.name())) {
            throw new IllegalArgumentException("User already exists");
        }
        UserEntity user = new UserEntity(request.name(), request.password());
        UserEntity saved = userRepository.save(user);
        // Create per-user view (safe name via lowercased username and id)
        String viewName = "snippets_user_" + saved.getId().toString().replace("-", "");
        String sql = "CREATE OR REPLACE VIEW " + viewName + " AS SELECT * FROM snippets WHERE user_id = '" + saved.getId() + "'";
        jdbcTemplate.execute(sql);
        return saved;
    }

    public Optional<UserEntity> login(@Valid LoginRequest request) {
        return userRepository.findByNameIgnoreCase(request.name())
                .filter(u -> u.getPassword().equals(request.password()));
    }

    public List<String> listUsers() {
        return userRepository.findAll().stream().map(UserEntity::getName).toList();
    }

    public record RegisterRequest(@NotBlank String name, @NotBlank String password) {}
    public record LoginRequest(@NotBlank String name, @NotBlank String password) {}
}


