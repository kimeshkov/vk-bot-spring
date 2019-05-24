package com.barinthecityshow.vkbot.dialog.chain;


import com.barinthecityshow.vkbot.chain.ChainElement;
import com.barinthecityshow.vkbot.dialog.QuestionAnswer;

import java.util.Optional;

public class QuestionAnswerChainElement implements ChainElement<QuestionAnswer> {

    private QuestionAnswer current;

    private QuestionAnswerChainElement next;


    public QuestionAnswerChainElement(QuestionAnswer current) {
        this.current = current;
    }

    @Override
    public Optional<ChainElement<QuestionAnswer>> next() {
        return Optional.ofNullable(next);
    }

    public void setNext(QuestionAnswerChainElement next) {
        this.next = next;
    }

    @Override
    public QuestionAnswer current() {
        return current;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private QuestionAnswer current;
        private QuestionAnswerChainElement next;

        public Builder current(QuestionAnswer current) {
            this.current = current;
            return this;
        }

        public Builder next(QuestionAnswerChainElement next) {
            this.next = next;
            return this;
        }

        public QuestionAnswerChainElement build() {
            QuestionAnswerChainElement result = new QuestionAnswerChainElement(current);
            result.setNext(next);
            return result;
        }
    }
}
