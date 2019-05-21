package com.barinthecityshow.vkbot.state;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ConcurrentMapWinnerState implements State<Integer, Object> {
    private final Map<Integer, Object> state = new ConcurrentHashMap<>();

    @Override
    public void put(Integer key, Object value) {
        state.put(key, value);
    }

    @Override
    public Object get(Integer key) {
        return state.get(key);
    }

    @Override
    public Object remove(Integer key) {
        return state.remove(key);
    }

    @Override
    public boolean containsKey(Integer key) {
        return state.containsKey(key);
    }
}
