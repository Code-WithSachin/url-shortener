package com.example.urlshortener.strategy;

import java.util.concurrent.atomic.AtomicLong;

public class AtomicSequenceGenerator implements SequenceGenerator {
    private final AtomicLong counter;

    public AtomicSequenceGenerator(long startValue) {
        this.counter = new AtomicLong(startValue);
    }

    @Override
    public long nextSequence() {
        return counter.incrementAndGet();
    }
}
