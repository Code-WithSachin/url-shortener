package com.example.urlshortener;

import com.example.urlshortener.dto.CreateUrlRequest;
import com.example.urlshortener.dto.ShortUrlResponse;
import com.example.urlshortener.dto.UrlStatsResponse;
import com.example.urlshortener.repository.InMemoryUrlRepository;
import com.example.urlshortener.repository.UrlRepository;
import com.example.urlshortener.service.UrlShortenerService;
import com.example.urlshortener.strategy.AtomicSequenceGenerator;
import com.example.urlshortener.strategy.Base62CounterStrategy;
import com.example.urlshortener.strategy.CodeGenerationStrategy;
import com.example.urlshortener.strategy.SequenceGenerator;
import com.example.urlshortener.validator.HttpUrlValidator;
import com.example.urlshortener.validator.UrlValidator;

import java.time.Duration;

public class Main {
    public static void main(String[] args) {
        String domain = "https://short.ly";
        UrlRepository repository = new InMemoryUrlRepository();
        SequenceGenerator sequenceGenerator = new AtomicSequenceGenerator(10_000_000L);
        CodeGenerationStrategy codeStrategy = new Base62CounterStrategy(sequenceGenerator);
        UrlValidator validator = new HttpUrlValidator("short.ly");
        UrlShortenerService service = new UrlShortenerService(
                repository,
                codeStrategy,
                validator,
                domain,
                Duration.ofDays(730)
        );

        System.out.println("=== 1. Happy Path: Auto-generated Short URL ===");
        CreateUrlRequest autoReq = new CreateUrlRequest(
                "https://en.wikipedia.org/wiki/System_design",
                null,
                Duration.ofDays(30),
                "user_101"
        );
        ShortUrlResponse autoRes = service.shortenUrl(autoReq);
        System.out.println("Original:  " + autoRes.getOriginalUrl());
        System.out.println("Short URL: " + autoRes.getFullShortUrl());
        System.out.println("Code:      " + autoRes.getShortCode());

        System.out.println("\n=== 2. Happy Path: Custom Alias ===");
        CreateUrlRequest customReq = new CreateUrlRequest(
                "https://github.com/google/guava",
                "google-guava",
                null,
                "user_101"
        );
        ShortUrlResponse customRes = service.shortenUrl(customReq);
        System.out.println("Custom Short URL: " + customRes.getFullShortUrl());

        System.out.println("\n=== 3. Resolution & Access Tracking ===");
        String resolvedUrl = service.resolveUrl(customRes.getShortCode());
        System.out.println("Redirecting to: " + resolvedUrl);

        // Record a second access
        service.resolveUrl(customRes.getShortCode());

        UrlStatsResponse stats = service.getUrlStats(customRes.getShortCode());
        System.out.println("Total Clicks:  " + stats.getClickCount());
        System.out.println("Last Accessed: " + stats.getLastAccessedAt().orElse(null));
        System.out.println("Status:        " + stats.getStatus());

        System.out.println("\n=== 4. Edge Cases: Validation and Conflicts ===");
        try {
            service.shortenUrl(new CreateUrlRequest("ftp://invalid-url.com"));
        } catch (Exception e) {
            System.out.println("Caught expected invalid protocol error: " + e.getMessage());
        }

        try {
            // Attempt to reuse already existing custom alias "google-guava"
            service.shortenUrl(new CreateUrlRequest("https://other-site.com", "google-guava"));
        } catch (Exception e) {
            System.out.println("Caught expected duplicate alias conflict: " + e.getMessage());
        }
    }
}
