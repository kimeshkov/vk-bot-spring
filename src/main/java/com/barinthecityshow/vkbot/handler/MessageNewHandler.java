package com.barinthecityshow.vkbot.handler;

import com.barinthecityshow.vkbot.chain.ChainElement;
import com.barinthecityshow.vkbot.dialog.QuestionAnswer;
import com.barinthecityshow.vkbot.dialog.chain.DialogChain;
import com.barinthecityshow.vkbot.service.VkApiService;
import com.barinthecityshow.vkbot.state.State;
import com.barinthecityshow.vkbot.statistics.Statistics;
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
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class MessageNewHandler extends AbstractNoResponseHandler {
    private static final Logger LOG = LoggerFactory.getLogger(MessageNewHandler.class);

    private static final int LIMIT = 300;
    private static final String STAT_MSG = "givemestat";

    private final List<String> startPhrases = Arrays.asList("Хочу стикер", "Хочу стикеры");
    private final static String STOP_PHRASE = "Стоп";

    private final State<Integer, ChainElement<QuestionAnswer>> questionAnswerState;
    private final State<Integer, Object> winnerState;
    private final DialogChain dialogChain;
    private final VkApiService vkApiService;
    private final Messages messages;

    private final AtomicInteger allWantStickerCounter = new AtomicInteger();

    @Autowired
    public MessageNewHandler(State<Integer, ChainElement<QuestionAnswer>> questionAnswerState,
                             State<Integer, Object> winnerState,
                             VkApiService vkApiService,
                             DialogChain dialogChain,
                             Messages messages) {
        this.questionAnswerState = questionAnswerState;
        this.winnerState = winnerState;
        this.vkApiService = vkApiService;
        this.dialogChain = dialogChain;
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
            handleUserInProgress(userId, msg);
        } else {
            handleNewUser(userId, msg);
        }
    }

    private void handleNewUser(int userId, String userMsg) {
        if (STAT_MSG.equals(userMsg) && vkApiService.isAdmin(userId)) {
            handleCollectStat(userId);
            return;
        }

        boolean isStartMsg = startPhrases.stream().anyMatch(s -> StringUtils.equalsIgnoreCase(userMsg, s));
        if (isStartMsg) {
            if (winnerState.containsKey(userId)) {
                handleAlreadyWinner(userId);
                return;
            }

            ChainElement<QuestionAnswer> first = dialogChain.getFirst();

            if (underLimit()) {
                vkApiService.sendMessage(userId, messages.getMessage("msg.welcome"));
                vkApiService.sendMessage(userId, first.current().getQuestion());
                allWantStickerCounter.incrementAndGet();
                questionAnswerState.put(userId, first);
            } else {
                vkApiService.sendMessage(userId, messages.getMessage("msg.limit"));
            }
        }
    }

    private void handleCollectStat(int userId) {
        String statMsg = Statistics.builder()
                .allWantSticker(allWantStickerCounter.get())
                .allUsersInDialog(questionAnswerState.size())
                .allWinners(winnerState.size())
                .build()
                .prettyPrint();

        vkApiService.sendMessage(userId, statMsg);

    }

    private void handleAlreadyWinner(int userId) {
        vkApiService.sendMessage(userId, messages.getMessage("msg.already.winner"));
    }

    private void handleUserInProgress(int userId, String msg) {
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
        return winnerState.size() < LIMIT;
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
