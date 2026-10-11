package com.example.urlshortener.strategy;

public interface CodeGenerationStrategy {
    String generateCode(String longUrl);
}
