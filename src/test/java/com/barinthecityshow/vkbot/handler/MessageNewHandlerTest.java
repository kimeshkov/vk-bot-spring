package com.barinthecityshow.vkbot.handler;

import com.barinthecityshow.vkbot.dialog.QuestionAnswer;
import com.barinthecityshow.vkbot.dialog.chain.DialogChain;
import com.barinthecityshow.vkbot.dialog.chain.QuestionAnswerChainElement;
import com.barinthecityshow.vkbot.service.VkApiService;
import com.barinthecityshow.vkbot.state.ConcurrentMapQuestionAnswerState;
import com.barinthecityshow.vkbot.state.ConcurrentMapWinnerState;
import com.barinthecityshow.vkbot.state.Counter;
import com.google.gson.JsonObject;
import com.vk.api.sdk.callback.objects.messages.CallbackMessageBase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Random;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MessageNewHandlerTest {
    @Mock
    private VkApiService vkApiService;

    @Mock
    private DialogChain dialogChain;

    private MessageNewHandler questionAnswerStateMachine;

    @Before
    public void init() throws Exception {
        reset(vkApiService, dialogChain);
        questionAnswerStateMachine = new MessageNewHandler(new ConcurrentMapQuestionAnswerState(),
                new ConcurrentMapWinnerState(),
                vkApiService,
                dialogChain,
                new Counter()
        );
    }

    private CallbackMessageBase mockMsg(Integer userId, String msg) {
        CallbackMessageBase callbackMessageBase = mock(CallbackMessageBase.class);

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("user_id", userId);
        jsonObject.addProperty("body", msg);

        when(callbackMessageBase.getObject()).thenReturn(jsonObject);

        return callbackMessageBase;
    }

    @Test
    public void shouldDoNothing_WhenNotStickerMsg() throws Exception {
        //arrange
        String msg = "Not sticker Msg";
        Integer userId = new Random().nextInt();

        //act
        questionAnswerStateMachine.handle(mockMsg(userId, msg));

        //assert
        verify(vkApiService, never()).sendMessage(anyInt(), anyString());

    }

    @Test
    public void shouldSendFirstQuestion_When_Хочу_стикер() throws Exception {
        //arrange
        String msg = "Хочу стикер";
        Integer userId = new Random().nextInt();

        QuestionAnswer questionAnswer = QuestionAnswer.builder()
                .question("What question")
                .build();

        QuestionAnswerChainElement chainElement = new QuestionAnswerChainElement(questionAnswer);

        when(dialogChain.getFirst()).thenReturn(chainElement);

        //act
        questionAnswerStateMachine.handle(mockMsg(userId, msg));

        //assert
        String result = Messages.WELCOME_MSG.getValue().concat(questionAnswer.getQuestion());
        verify(vkApiService).sendMessage(userId, result);

    }

    @Test
    public void shouldSendFirstQuestion_When_Хочу_Стикеры() throws Exception {
        //arrange
        String msg = "Хочу стикеры";
        Integer userId = new Random().nextInt();

        QuestionAnswer questionAnswer = QuestionAnswer.builder()
                .question("What question")
                .build();

        QuestionAnswerChainElement chainElement = new QuestionAnswerChainElement(questionAnswer);

        when(dialogChain.getFirst()).thenReturn(chainElement);

        //act
        questionAnswerStateMachine.handle(mockMsg(userId, msg));

        //assert
        String result = Messages.WELCOME_MSG.getValue().concat(questionAnswer.getQuestion());
        verify(vkApiService).sendMessage(userId, result);

    }

    @Test
    public void shouldSendAlreadyWinner_WhenUserTryAgainAfterWin() throws Exception {
        //arrange
        Integer userId = new Random().nextInt();

        String msg = "Хочу стикер";

        QuestionAnswer questionAnswer = QuestionAnswer.builder()
                .question("What question")
                .addCorrectAnswer("Correct")
                .build();

        QuestionAnswerChainElement chainElement = new QuestionAnswerChainElement(questionAnswer);

        ConcurrentMapWinnerState winnerState = new ConcurrentMapWinnerState();
        winnerState.put(userId, chainElement);
        questionAnswerStateMachine = new MessageNewHandler(new ConcurrentMapQuestionAnswerState(),
                winnerState,
                vkApiService,
                dialogChain,
                new Counter());

        //act
        questionAnswerStateMachine.handle(mockMsg(userId, msg));

        //assert
        verify(vkApiService, times(0)).sendMessage(userId, Messages.WRONG_ANS_MSG.getValue());

        verify(vkApiService, times(1)).sendMessage(userId, Messages.ALREADY_WINNER_MSG.getValue());

    }

    @Test
    public void shouldSendTryAgain_WhenWrongAnswer() throws Exception {
        //arrange
        String msg = "Хочу стикер";
        String answer = "Wrong";
        Integer userId = new Random().nextInt();

        QuestionAnswer questionAnswer = QuestionAnswer.builder()
                .question("What question")
                .addCorrectAnswer("Correct")
                .build();

        QuestionAnswerChainElement chainElement = new QuestionAnswerChainElement(questionAnswer);

        when(dialogChain.getFirst()).thenReturn(chainElement);

        //act
        questionAnswerStateMachine.handle(mockMsg(userId, msg));
        questionAnswerStateMachine.handle(mockMsg(userId, answer));

        //assert
        verify(vkApiService).sendMessage(userId, Messages.WRONG_ANS_MSG.getValue());

    }

    @Test
    public void shouldSendWinMsg_WhenCorrectAnswer_AndNoMoreQuestions() throws Exception {
        //arrange
        String msg = "Хочу стикер";
        String answer = "Correct";
        Integer userId = new Random().nextInt();

        QuestionAnswer questionAnswer = QuestionAnswer.builder()
                .question("What question")
                .addCorrectAnswer("Correct")
                .build();

        QuestionAnswerChainElement chainElement = new QuestionAnswerChainElement(questionAnswer);


        when(vkApiService.isSubscribed(userId)).thenReturn(true);
        when(dialogChain.getFirst()).thenReturn(chainElement);

        //act
        questionAnswerStateMachine.handle(mockMsg(userId, msg));
        questionAnswerStateMachine.handle(mockMsg(userId, answer));

        //assert
        verify(vkApiService).sendMessage(userId, Messages.WIN_MSG.getValue());

    }

    @Test
    public void shouldSendSubscribeMsg_WhenCorrectAnswerAndUserNotSubscribed() throws Exception {
        //arrange
        String msg = "Хочу стикер";
        String answer = "Correct";
        Integer userId = new Random().nextInt();

        QuestionAnswer questionAnswer = QuestionAnswer.builder()
                .question("What question")
                .addCorrectAnswer("Correct")
                .build();

        QuestionAnswerChainElement chainElement = new QuestionAnswerChainElement(questionAnswer);


        when(vkApiService.isSubscribed(userId)).thenReturn(false);
        when(dialogChain.getFirst()).thenReturn(chainElement);

        //act
        questionAnswerStateMachine.handle(mockMsg(userId, msg));
        questionAnswerStateMachine.handle(mockMsg(userId, answer));

        //assert
        verify(vkApiService).sendMessage(userId, Messages.SUBSCRIBE_MSG.getValue());
    }

    @Test
    public void не_Должно_Быть_Разницы_Между_Е_И_Ё() throws Exception {
        //arrange
        String msg = "Хочу стикер";
        String answer = "небо";
        Integer userId = new Random().nextInt();

        QuestionAnswer questionAnswer = QuestionAnswer.builder()
                .question("What question")
                .addCorrectAnswer("Нёбо")
                .build();

        QuestionAnswerChainElement chainElement = new QuestionAnswerChainElement(questionAnswer);


        when(vkApiService.isSubscribed(userId)).thenReturn(true);
        when(dialogChain.getFirst()).thenReturn(chainElement);

        //act
        questionAnswerStateMachine.handle(mockMsg(userId, msg));
        questionAnswerStateMachine.handle(mockMsg(userId, answer));

        //assert
        verify(vkApiService).sendMessage(userId, Messages.WIN_MSG.getValue());

    }

}