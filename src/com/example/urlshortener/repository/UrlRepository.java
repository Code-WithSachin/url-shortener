package com.example.urlshortener.repository;

import com.example.urlshortener.model.ShortUrl;

import java.time.Instant;
import java.util.Optional;

public interface UrlRepository {
    ShortUrl save(ShortUrl shortUrl);
    Optional<ShortUrl> findByShortCode(String shortCode);
    boolean existsByShortCode(String shortCode);
    void incrementClickCount(String shortCode, Instant accessTime);
    void delete(String shortCode);
}
