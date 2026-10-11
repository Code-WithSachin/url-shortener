package com.example.urlshortener.exception;

public class DuplicateKeyException extends UrlShortenerException {
    public DuplicateKeyException(String message) {
        super(message);
    }
}
