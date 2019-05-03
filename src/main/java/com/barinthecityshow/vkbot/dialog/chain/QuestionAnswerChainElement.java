package com.barinthecityshow.vkbot.dialog.chain;


import com.barinthecityshow.vkbot.chain.ChainElement;
import com.barinthecityshow.vkbot.dialog.QuestionAnswer;

import java.util.Optional;

public class QuestionAnswerChainElement implements ChainElement<QuestionAnswer> {

    private QuestionAnswerChainElement next;

    private QuestionAnswer current;

    public QuestionAnswerChainElement(QuestionAnswer current) {
        this.current = current;
    }

    @Override
    public Optional<ChainElement<QuestionAnswer>> next() {
        return Optional.ofNullable(next);
    }

    @Override
    public QuestionAnswer current() {
        return current;
    }
}
