package com.barinthecityshow.vkbot.state;

import com.barinthecityshow.vkbot.chain.ChainElement;
import com.barinthecityshow.vkbot.dialog.QuestionAnswer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public enum ConcurrentMapState implements State<Integer, ChainElement<QuestionAnswer>> {
    INSTANCE;

    private Map<Integer, ChainElement<QuestionAnswer>> state = new ConcurrentHashMap<>();


    @Override
    public void put(Integer key, ChainElement<QuestionAnswer> value) {
        state.put(key, value);
    }

    @Override
    public ChainElement<QuestionAnswer> get(Integer key) {
        return state.get(key);
    }

    @Override
    public ChainElement<QuestionAnswer> remove(Integer key) {
        return state.remove(key);
    }

    @Override
    public boolean containsKey(Integer key) {
        return state.containsKey(key);
    }
}
