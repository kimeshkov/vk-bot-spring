package com.barinthecityshow.vkbot.handler;

import com.barinthecityshow.vkbot.chain.ChainElement;
import com.barinthecityshow.vkbot.dialog.QuestionAnswer;
import com.barinthecityshow.vkbot.dialog.chain.DialogChain;
import com.barinthecityshow.vkbot.service.VkApiService;
import com.barinthecityshow.vkbot.state.ConcurrentMapState;
import com.barinthecityshow.vkbot.state.State;
import com.vk.api.sdk.callback.objects.messages.CallbackMessageBase;
import com.vk.api.sdk.callback.objects.messages.CallbackMessageType;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MessageNewHandler extends AbstractNoResponseHandler {
    private final State<Integer, ChainElement<QuestionAnswer>> state = ConcurrentMapState.INSTANCE;
    private final DialogChain dialogChain;
    private final VkApiService vkApiService;

    @Autowired
    public MessageNewHandler(VkApiService vkApiService, DialogChain dialogChain) {
        this.vkApiService = vkApiService;
        this.dialogChain = dialogChain;
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

        if (!state.containsKey(userId)) {
            if (!StringUtils.equalsIgnoreCase(msg, Messages.START_MSG.getValue())) {
                return;
            }
            if (!vkApiService.isSubscribed(userId)) {
                handleNotSubscribed(userId);
                return;
            }

            ChainElement<QuestionAnswer> first = dialogChain.getFirst();
            handleNew(userId, first.current());
            state.put(userId, first);

        } else {
            ChainElement<QuestionAnswer> chainElement = state.get(userId);
            if (isStopMsg(msg)) {
                handleStop(userId);
                return;
            }
            if (isCorrectAnswer(msg, chainElement.current())) {
                if (chainElement.next().isPresent()) {
                    ChainElement<QuestionAnswer> next = chainElement.next().get();
                    handleNext(userId, next.current());
                    state.put(userId, next);
                } else {
                    handleWin(userId);
                }
            } else {
                handleWrong(userId);
            }
        }


    }

    private String prepareMsg(String userMsg) {
        return StringUtils.replaceChars(userMsg.toUpperCase(), 'Ё', 'Е');
    }

    private void handleNotSubscribed(Integer userId) {
        vkApiService.sendMessage(userId, Messages.SUBSCRIBE_MSG.getValue());
    }

    private void handleNew(Integer userId, QuestionAnswer first) {
        String msg = Messages.WELCOME_MSG.getValue().concat(first.getQuestion());
        vkApiService.sendMessage(userId, msg);
    }

    private boolean isStopMsg(String msg) {
        return StringUtils.equalsIgnoreCase(msg, Messages.STOP_MSG.getValue());
    }

    private boolean isCorrectAnswer(String msg, QuestionAnswer questionAnswer) {
        return questionAnswer.getCorrectAnswers()
                .stream()
                .anyMatch(s -> StringUtils.equalsIgnoreCase(prepareMsg(s), prepareMsg(msg)));
    }

    private void handleNext(Integer userId, QuestionAnswer next) {
        String msg = Messages.CORRECT_ANS_MSG.getValue().concat(next.getQuestion());
        vkApiService.sendMessage(userId, msg);
    }

    private void handleWrong(Integer userId) {
        vkApiService.sendMessage(userId, Messages.WRONG_ANS_MSG.getValue());
    }

    private void handleWin(Integer userId) {
        vkApiService.sendMessage(userId, Messages.WIN_MSG.getValue());
        state.remove(userId);
    }

    private void handleStop(Integer userId) {
        vkApiService.sendMessage(userId, Messages.BYE_MSG.getValue());
        state.remove(userId);
    }
}
