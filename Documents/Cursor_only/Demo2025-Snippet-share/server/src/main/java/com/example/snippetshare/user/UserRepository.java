package com.example.snippetshare.user;

import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class UserRepository {
    private final Map<String, User> nameToUser = new ConcurrentHashMap<>();

    public Optional<User> findByName(String name) {
        return Optional.ofNullable(nameToUser.get(name.toLowerCase()));
    }

    public User save(User user) {
        nameToUser.put(user.getName().toLowerCase(), user);
        return user;
    }
}


