package com.barinthecityshow.vkbot.handler;

import com.barinthecityshow.vkbot.chain.ChainElement;
import com.barinthecityshow.vkbot.dialog.QuestionAnswer;
import com.barinthecityshow.vkbot.dialog.chain.DialogChain;
import com.barinthecityshow.vkbot.service.VkApiService;
import com.barinthecityshow.vkbot.state.Counter;
import com.barinthecityshow.vkbot.state.State;
import com.vk.api.sdk.callback.objects.messages.CallbackMessageBase;
import com.vk.api.sdk.callback.objects.messages.CallbackMessageType;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MessageNewHandler extends AbstractNoResponseHandler {
    private static final Logger LOG = LoggerFactory.getLogger(MessageNewHandler.class);

    private static final int LIMIT = 300;

    private final State<Integer, ChainElement<QuestionAnswer>> questionAnswerState;
    private final State<Integer, Object> winnerState;
    private final DialogChain dialogChain;
    private final VkApiService vkApiService;
    private final Counter counter;

    @Autowired
    public MessageNewHandler(State<Integer, ChainElement<QuestionAnswer>> questionAnswerState,
                             State<Integer, Object> winnerState,
                             VkApiService vkApiService,
                             DialogChain dialogChain,
                             Counter counter) {
        this.questionAnswerState = questionAnswerState;
        this.winnerState = winnerState;
        this.vkApiService = vkApiService;
        this.dialogChain = dialogChain;
        this.counter = counter;
    }

    @Override
    public CallbackMessageType getType() {
        return CallbackMessageType.MESSAGE_NEW;
    }

    @Override
    public void doHandle(CallbackMessageBase message) {
        int userId = message.getObject().getAsJsonPrimitive("user_id").getAsInt();
        String userMsg = message.getObject().getAsJsonPrimitive("body").getAsString();

        String msg = prepareMsg(userMsg);

        if (questionAnswerState.containsKey(userId)) {
            handleRegisteredUser(userId, msg);
        } else {
            handleNewUser(userId, msg);
        }
    }

    private void handleNewUser(int userId, String userMsg) {
        if (!StringUtils.equalsIgnoreCase(userMsg, Messages.START_MSG.getValue())) {
            return;
        }

        if (winnerState.containsKey(userId)) {
            handleAlreadyWinner(userId);
            return;
        }

        ChainElement<QuestionAnswer> first = dialogChain.getFirst();

        if (underLimit()) {
            String msg = Messages.WELCOME_MSG.getValue().concat(first.current().getQuestion());
            vkApiService.sendMessage(userId, msg);
            questionAnswerState.put(userId, first);
        } else {
            vkApiService.sendMessage(userId, Messages.LIMIT_MSG.getValue());
        }
    }

    private void handleAlreadyWinner(int userId) {
        String msg = Messages.ALREADY_WINNER_MSG.getValue();
        vkApiService.sendMessage(userId, msg);
    }

    private void handleRegisteredUser(int userId, String msg) {
        if (isStopMsg(msg)) {
            handleStop(userId);
            return;
        }

        ChainElement<QuestionAnswer> chainElement = questionAnswerState.get(userId);

        if (isCorrectAnswer(msg, chainElement.current())) {
            if (chainElement.next().isPresent()) {
                ChainElement<QuestionAnswer> next = chainElement.next().get();
                handleNext(userId, next);

            } else {
                handleWin(userId);
            }
        } else {
            handleWrong(userId);
        }
    }

    private String prepareMsg(String userMsg) {
        return StringUtils.replaceChars(userMsg.toUpperCase(), 'Ё', 'Е');
    }

    private boolean underLimit() {
        return counter.get() < LIMIT;
    }

    private boolean isStopMsg(String msg) {
        return StringUtils.equalsIgnoreCase(msg, Messages.STOP_MSG.getValue());
    }

    private boolean isCorrectAnswer(String msg, QuestionAnswer questionAnswer) {
        return questionAnswer.getCorrectAnswers()
                .stream()
                .anyMatch(s -> StringUtils.equalsIgnoreCase(prepareMsg(s), prepareMsg(msg)));
    }

    private void handleNext(Integer userId, ChainElement<QuestionAnswer> next) {
        String msg = Messages.CORRECT_ANS_MSG.getValue().concat(next.current().getQuestion());
        vkApiService.sendMessage(userId, msg);//todo
        questionAnswerState.put(userId, next);
    }

    private void handleWrong(Integer userId) {
        vkApiService.sendMessage(userId, Messages.WRONG_ANS_MSG.getValue());
    }

    private void handleWin(Integer userId) {
        vkApiService.sendMessage(userId, Messages.WIN_MSG.getValue());
        vkApiService.openPromoStickerPack(userId);

        int current = counter.incrementAndGet();
        LOG.info("Handle winner. Total: {}", current);
        questionAnswerState.remove(userId);
        winnerState.put(userId, new Object());

        if (!vkApiService.isSubscribed(userId)) {
            handleNotSubscribed(userId);
        }
    }

    private void handleNotSubscribed(Integer userId) {
        vkApiService.sendMessage(userId, Messages.SUBSCRIBE_MSG.getValue());
    }

    private void handleStop(Integer userId) {
        vkApiService.sendMessage(userId, Messages.BYE_MSG.getValue());
        questionAnswerState.remove(userId);
    }
}
