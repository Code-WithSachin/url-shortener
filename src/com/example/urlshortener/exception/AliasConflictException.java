package com.example.urlshortener.exception;

public class AliasConflictException extends UrlShortenerException {
    public AliasConflictException(String message) {
        super(message);
    }

    public AliasConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
