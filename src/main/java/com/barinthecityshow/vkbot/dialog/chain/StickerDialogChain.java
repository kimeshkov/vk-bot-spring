package com.barinthecityshow.vkbot.dialog.chain;


import com.barinthecityshow.vkbot.chain.ChainElement;
import com.barinthecityshow.vkbot.dialog.QuestionAnswer;

import java.util.List;
import java.util.Random;

public class StickerDialogChain implements DialogChain {
    private final Random random;
    private final List<QuestionAnswer> firstLevelQuestionAnswers;
    private final List<QuestionAnswer> secondLevelQuestionAnswers;
    private final List<QuestionAnswer> thirdLevelQuestionAnswers;


    public StickerDialogChain(List<QuestionAnswer> firstLevelQuestionAnswers,
                              List<QuestionAnswer> secondLevelQuestionAnswers,
                              List<QuestionAnswer> thirdLevelQuestionAnswers) {
        random = new Random();
        this.firstLevelQuestionAnswers = firstLevelQuestionAnswers;
        this.secondLevelQuestionAnswers = secondLevelQuestionAnswers;
        this.thirdLevelQuestionAnswers = thirdLevelQuestionAnswers;
    }

    @Override
    public ChainElement<QuestionAnswer> getFirst() {
        return randomQuestion();
    }

    private ChainElement<QuestionAnswer> randomQuestion() {
        return QuestionAnswerChainElement.builder()
                .current(getRandom(firstLevelQuestionAnswers))
                .next(QuestionAnswerChainElement.builder()
                        .current(getRandom(secondLevelQuestionAnswers))
                        .next(QuestionAnswerChainElement.builder()
                                .current(getRandom(thirdLevelQuestionAnswers))
                                .build())
                        .build())
                .build();

    }

    private QuestionAnswer getRandom(List<QuestionAnswer> questionAnswers) {
        return questionAnswers.get(random.nextInt(questionAnswers.size()));
    }


}
