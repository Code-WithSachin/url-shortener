package com.example.urlshortener.exception;

public class UrlExpiredException extends UrlShortenerException {
    public UrlExpiredException(String message) {
        super(message);
    }
}
