package com.example.snippetshare.snippet;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class SnippetRepository {

    private final Map<String, Snippet> idToSnippet = new ConcurrentHashMap<>();

    public Snippet save(Snippet snippet) {
        idToSnippet.put(snippet.getId(), snippet);
        return snippet;
    }

    public Optional<Snippet> findById(String id) {
        return Optional.ofNullable(idToSnippet.get(id));
    }

    public List<Snippet> findAll() {
        return Collections.unmodifiableList(new ArrayList<>(idToSnippet.values()));
    }

    public boolean deleteById(String id) {
        return idToSnippet.remove(id) != null;
    }
}


