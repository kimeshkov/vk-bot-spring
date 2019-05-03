package com.barinthecityshow.vkbot.chain;

import java.util.Optional;

public interface ChainElement<T> {
    Optional<ChainElement<T>> next();

    T current();
}
