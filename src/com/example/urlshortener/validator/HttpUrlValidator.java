package com.example.urlshortener.validator;

import com.example.urlshortener.exception.InvalidUrlException;

import java.net.URI;
import java.util.Objects;
import java.util.Set;

public class HttpUrlValidator implements UrlValidator {
    private static final Set<String> ALLOWED_SCHEMES = Set.of("http", "https");
    private final String ownDomain;

    public HttpUrlValidator(String ownDomain) {
        this.ownDomain = Objects.requireNonNull(ownDomain).toLowerCase();
    }

    @Override
    public void validate(String url) {
        if (url == null || url.isBlank()) {
            throw new InvalidUrlException("Destination URL cannot be empty");
        }
        try {
            URI uri = URI.create(url);
            String scheme = uri.getScheme();
            if (scheme == null || !ALLOWED_SCHEMES.contains(scheme.toLowerCase())) {
                throw new InvalidUrlException("Invalid URL protocol. Only HTTP/HTTPS supported: " + url);
            }
            if (uri.getHost() == null || uri.getHost().isBlank()) {
                throw new InvalidUrlException("URL is missing a valid host: " + url);
            }
            if (uri.getHost().equalsIgnoreCase(ownDomain)) {
                throw new InvalidUrlException("Self-referential shortening is prohibited: " + url);
            }
        } catch (IllegalArgumentException e) {
            throw new InvalidUrlException("Malformed destination URI syntax: " + url);
        }
    }
}
