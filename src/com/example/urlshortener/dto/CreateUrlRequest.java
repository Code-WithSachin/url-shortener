package com.example.urlshortener.dto;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;

public class CreateUrlRequest {
    private final String longUrl;
    private final String customAlias;
    private final Duration ttl;
    private final String userId;

    public CreateUrlRequest(String longUrl, String customAlias, Duration ttl, String userId) {
        this.longUrl = Objects.requireNonNull(longUrl, "longUrl cannot be null");
        this.customAlias = customAlias;
        this.ttl = ttl;
        this.userId = userId;
    }

    public CreateUrlRequest(String longUrl) {
        this(longUrl, null, null, null);
    }

    public CreateUrlRequest(String longUrl, String customAlias) {
        this(longUrl, customAlias, null, null);
    }

    public String getLongUrl() {
        return longUrl;
    }

    public Optional<String> getCustomAlias() {
        return Optional.ofNullable(customAlias);
    }

    public Optional<Duration> getTtl() {
        return Optional.ofNullable(ttl);
    }

    public Optional<String> getUserId() {
        return Optional.ofNullable(userId);
    }
}
