package com.example.urlshortener.strategy;

import java.util.Objects;

public class Base62CounterStrategy implements CodeGenerationStrategy {
    private static final String BASE62_ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int BASE = BASE62_ALPHABET.length();
    private static final int CODE_LENGTH = 7;

    private final SequenceGenerator sequenceGenerator;

    public Base62CounterStrategy(SequenceGenerator sequenceGenerator) {
        this.sequenceGenerator = Objects.requireNonNull(sequenceGenerator);
    }

    @Override
    public String generateCode(String longUrl) {
        long id = sequenceGenerator.nextSequence();
        return encodeBase62(id);
    }

    private String encodeBase62(long num) {
        StringBuilder sb = new StringBuilder();
        while (num > 0) {
            int remainder = (int) (num % BASE);
            sb.append(BASE62_ALPHABET.charAt(remainder));
            num /= BASE;
        }

        while (sb.length() < CODE_LENGTH) {
            sb.append(BASE62_ALPHABET.charAt(0));
        }

        return sb.reverse().toString();
    }
}
