package com.barinthecityshow.vkbot.state;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
public class Counter {
    private final AtomicInteger count = new AtomicInteger();

    public int incrementAndGet() {
        return count.incrementAndGet();
    }

    public int get() {
        return count.get();
    }

}
