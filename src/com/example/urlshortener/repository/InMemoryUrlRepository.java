package com.example.urlshortener.repository;

import com.example.urlshortener.exception.DuplicateKeyException;
import com.example.urlshortener.model.ShortUrl;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryUrlRepository implements UrlRepository {
    private final ConcurrentHashMap<String, ShortUrl> storage = new ConcurrentHashMap<>();

    @Override
    public ShortUrl save(ShortUrl shortUrl) {
        ShortUrl existing = storage.putIfAbsent(shortUrl.getShortCode(), shortUrl);
        if (existing != null) {
            throw new DuplicateKeyException("Duplicate short code constraint violation: " + shortUrl.getShortCode());
        }
        return shortUrl;
    }

    @Override
    public Optional<ShortUrl> findByShortCode(String shortCode) {
        return Optional.ofNullable(storage.get(shortCode));
    }

    @Override
    public boolean existsByShortCode(String shortCode) {
        return storage.containsKey(shortCode);
    }

    @Override
    public void incrementClickCount(String shortCode, Instant accessTime) {
        ShortUrl url = storage.get(shortCode);
        if (url != null) {
            url.recordAccess(accessTime);
        }
    }

    @Override
    public void delete(String shortCode) {
        storage.remove(shortCode);
    }
}
