package com.barinthecityshow.vkbot.handler;

import com.barinthecityshow.vkbot.chain.ChainElement;
import com.barinthecityshow.vkbot.dialog.QuestionAnswer;
import com.barinthecityshow.vkbot.dialog.chain.DialogChain;
import com.barinthecityshow.vkbot.service.VkApiService;
import com.barinthecityshow.vkbot.state.Counter;
import com.barinthecityshow.vkbot.state.State;
import com.vk.api.sdk.callback.objects.messages.CallbackMessageBase;
import com.vk.api.sdk.callback.objects.messages.CallbackMessageType;
import com.vk.api.sdk.exceptions.ApiException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class MessageNewHandler extends AbstractNoResponseHandler {
    private static final Logger LOG = LoggerFactory.getLogger(MessageNewHandler.class);

    private static final int LIMIT = 300;

    private final List<String> startPhrases = Arrays.asList("Хочу стикер", "Хочу стикеры");
    private final static String STOP_PHRASE = "Стоп";

    private final State<Integer, ChainElement<QuestionAnswer>> questionAnswerState;
    private final State<Integer, Object> winnerState;
    private final DialogChain dialogChain;
    private final VkApiService vkApiService;
    private final Counter counter;
    private final Messages messages;

    @Autowired
    public MessageNewHandler(State<Integer, ChainElement<QuestionAnswer>> questionAnswerState,
                             State<Integer, Object> winnerState,
                             VkApiService vkApiService,
                             DialogChain dialogChain,
                             Counter counter,
                             Messages messages) {
        this.questionAnswerState = questionAnswerState;
        this.winnerState = winnerState;
        this.vkApiService = vkApiService;
        this.dialogChain = dialogChain;
        this.counter = counter;
        this.messages = messages;
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
        boolean isStartMsg = startPhrases.stream().anyMatch(s -> StringUtils.equalsIgnoreCase(userMsg, s));

        if (!isStartMsg) {
            return;
        }

        if (winnerState.containsKey(userId)) {
            handleAlreadyWinner(userId);
            return;
        }

        ChainElement<QuestionAnswer> first = dialogChain.getFirst();

        if (underLimit()) {
            vkApiService.sendMessage(userId, messages.getMessage("msg.welcome"));
            vkApiService.sendMessage(userId, first.current().getQuestion());
            questionAnswerState.put(userId, first);
        } else {
            vkApiService.sendMessage(userId, messages.getMessage("msg.limit"));
        }
    }

    private void handleAlreadyWinner(int userId) {
        vkApiService.sendMessage(userId, messages.getMessage("msg.already.winner"));
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
        return StringUtils.equalsIgnoreCase(msg, STOP_PHRASE);
    }

    private boolean isCorrectAnswer(String msg, QuestionAnswer questionAnswer) {
        return questionAnswer.getCorrectAnswers()
                .stream()
                .anyMatch(s -> StringUtils.equalsIgnoreCase(prepareMsg(s), prepareMsg(msg)));
    }

    private void handleNext(Integer userId, ChainElement<QuestionAnswer> next) {
        vkApiService.sendMessage(userId, messages.getMessage("msg.correct.answer"));
        vkApiService.sendMessage(userId, next.current().getQuestion());

        questionAnswerState.put(userId, next);
    }

    private void handleWrong(Integer userId) {
        vkApiService.sendMessage(userId, messages.getMessage("msg.wrong.answer"));
    }

    private void handleWin(Integer userId) {
        try {
            vkApiService.openPromoStickerPack(userId);
            vkApiService.sendMessage(userId, messages.getMessage("msg.win"));

            int current = counter.incrementAndGet();
            LOG.info("Handle winner. Total: {}", current);
            questionAnswerState.remove(userId);
            winnerState.put(userId, new Object());

            vkApiService.sendMessage(userId, messages.getMessage("msg.bye"));
            if (!vkApiService.isSubscribed(userId)) {
                vkApiService.sendMessage(userId, messages.getMessage("msg.subscribe.vk"));
            }
        } catch (ApiException e) {
            vkApiService.sendMessage(userId, messages.getMessage("msg.limit"));
            questionAnswerState.remove(userId);
            LOG.error("Error while promo", e);
        }
    }

    private void handleStop(Integer userId) {
        vkApiService.sendMessage(userId, messages.getMessage("msg.come.later"));
        questionAnswerState.remove(userId);
    }
}
