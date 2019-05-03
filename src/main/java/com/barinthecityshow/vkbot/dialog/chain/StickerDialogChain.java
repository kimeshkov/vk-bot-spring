package com.barinthecityshow.vkbot.dialog.chain;


import com.barinthecityshow.vkbot.chain.ChainElement;
import com.barinthecityshow.vkbot.dialog.QuestionAnswer;

import java.util.List;
import java.util.Random;

public class StickerDialogChain implements DialogChain {
    private final Random random;
    private final List<QuestionAnswer> questionAnswers;


    public StickerDialogChain(List<QuestionAnswer> questionAnswers) {
        random = new Random();
        this.questionAnswers = questionAnswers;
    }

    @Override
    public ChainElement<QuestionAnswer> getFirst() {
        return randomQuestion();
    }

    private ChainElement<QuestionAnswer> randomQuestion() {
        return new QuestionAnswerChainElement(questionAnswers.get(random.nextInt(questionAnswers.size())));

    }



}
