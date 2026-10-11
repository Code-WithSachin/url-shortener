package com.example.urlshortener.exception;

public class UrlNotFoundException extends UrlShortenerException {
    public UrlNotFoundException(String message) {
        super(message);
    }
}
