package com.example.urlshortener.service;

import com.example.urlshortener.dto.CreateUrlRequest;
import com.example.urlshortener.dto.ShortUrlResponse;
import com.example.urlshortener.dto.UrlStatsResponse;
import com.example.urlshortener.exception.AliasConflictException;
import com.example.urlshortener.exception.DuplicateKeyException;
import com.example.urlshortener.exception.InvalidAliasException;
import com.example.urlshortener.exception.UrlExpiredException;
import com.example.urlshortener.exception.UrlNotFoundException;
import com.example.urlshortener.model.ShortUrl;
import com.example.urlshortener.model.UrlStatus;
import com.example.urlshortener.repository.UrlRepository;
import com.example.urlshortener.strategy.CodeGenerationStrategy;
import com.example.urlshortener.validator.UrlValidator;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

public class UrlShortenerService {

    private static final Pattern ALIAS_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]{4,16}$");
    private static final Set<String> RESERVED_KEYWORDS = Set.of("api", "health", "metrics", "admin", "static");

    private final UrlRepository urlRepository;
    private final CodeGenerationStrategy codeGenerator;
    private final UrlValidator urlValidator;
    private final String baseDomain;
    private final Duration defaultTtl;

    public UrlShortenerService(
            UrlRepository urlRepository,
            CodeGenerationStrategy codeGenerator,
            UrlValidator urlValidator,
            String baseDomain,
            Duration defaultTtl) {
        this.urlRepository = Objects.requireNonNull(urlRepository, "urlRepository cannot be null");
        this.codeGenerator = Objects.requireNonNull(codeGenerator, "codeGenerator cannot be null");
        this.urlValidator = Objects.requireNonNull(urlValidator, "urlValidator cannot be null");
        this.baseDomain = Objects.requireNonNull(baseDomain, "baseDomain cannot be null");
        this.defaultTtl = Objects.requireNonNull(defaultTtl, "defaultTtl cannot be null");
    }

    public ShortUrlResponse shortenUrl(CreateUrlRequest request) {
        Objects.requireNonNull(request, "request cannot be null");
        urlValidator.validate(request.getLongUrl());

        String shortCode;
        if (request.getCustomAlias().isPresent()) {
            String alias = request.getCustomAlias().get().trim();
            validateCustomAlias(alias);

            if (urlRepository.existsByShortCode(alias)) {
                throw new AliasConflictException("Alias already in use: " + alias);
            }
            shortCode = alias;
        } else {
            shortCode = codeGenerator.generateCode(request.getLongUrl());
        }

        Instant now = Instant.now();
        Instant expiresAt = request.getTtl()
                .map(now::plus)
                .orElseGet(() -> now.plus(defaultTtl));

        ShortUrl shortUrl = ShortUrl.builder()
                .shortCode(shortCode)
                .originalUrl(request.getLongUrl())
                .userId(request.getUserId().orElse(null))
                .createdAt(now)
                .expiresAt(expiresAt)
                .status(UrlStatus.ACTIVE)
                .clickCount(0L)
                .build();

        try {
            urlRepository.save(shortUrl);
        } catch (DuplicateKeyException e) {
            throw new AliasConflictException("Collision detected for short code: " + shortCode, e);
        }

        String fullShortUrl = String.format("%s/%s", baseDomain, shortCode);
        return new ShortUrlResponse(shortCode, fullShortUrl, shortUrl.getOriginalUrl(), expiresAt);
    }

    public String resolveUrl(String shortCode) {
        if (shortCode == null || shortCode.isBlank()) {
            throw new UrlNotFoundException("Short code must not be empty");
        }

        ShortUrl shortUrl = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException("No URL mapping found for short code: " + shortCode));

        Instant now = Instant.now();
        if (shortUrl.getStatus() != UrlStatus.ACTIVE || shortUrl.isExpired(now)) {
            throw new UrlExpiredException("Short URL has expired or is inactive: " + shortCode);
        }

        urlRepository.incrementClickCount(shortCode, now);
        return shortUrl.getOriginalUrl();
    }

    public UrlStatsResponse getUrlStats(String shortCode) {
        ShortUrl shortUrl = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException("No URL mapping found for short code: " + shortCode));

        return new UrlStatsResponse(
                shortUrl.getShortCode(),
                shortUrl.getOriginalUrl(),
                shortUrl.getClickCount(),
                shortUrl.getCreatedAt(),
                shortUrl.getExpiresAt(),
                shortUrl.getLastAccessedAt().orElse(null),
                shortUrl.getStatus()
        );
    }

    public void revokeUrl(String shortCode, String requesterId) {
        ShortUrl shortUrl = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException("No URL mapping found for short code: " + shortCode));

        if (shortUrl.getUserId().isPresent() && !shortUrl.getUserId().get().equals(requesterId)) {
            throw new SecurityException("User not authorized to revoke this link");
        }

        shortUrl.revoke();
    }

    private void validateCustomAlias(String alias) {
        if (!ALIAS_PATTERN.matcher(alias).matches()) {
            throw new InvalidAliasException("Custom alias must be 4-16 alphanumeric characters ([a-zA-Z0-9_-]): " + alias);
        }
        if (RESERVED_KEYWORDS.contains(alias.toLowerCase())) {
            throw new InvalidAliasException("Custom alias uses a reserved keyword: " + alias);
        }
    }
}
