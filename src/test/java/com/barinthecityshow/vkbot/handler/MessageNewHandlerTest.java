package com.barinthecityshow.vkbot.handler;

import com.barinthecityshow.vkbot.dialog.QuestionAnswer;
import com.barinthecityshow.vkbot.dialog.chain.DialogChain;
import com.barinthecityshow.vkbot.dialog.chain.QuestionAnswerChainElement;
import com.barinthecityshow.vkbot.service.VkApiService;
import com.barinthecityshow.vkbot.state.ConcurrentMapQuestionAnswerState;
import com.barinthecityshow.vkbot.state.ConcurrentMapWinnerState;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import com.vk.api.sdk.callback.objects.messages.CallbackMessageBase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;
import org.springframework.lang.Nullable;

import java.util.Locale;
import java.util.Map;
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
    private static final Map<String, String> map = ImmutableMap.<String, String>builder()
            .put("msg.welcome", "Привет! Я бот бара в большом городе. Раз хочешь стикер, ответь на три вопроса. Итак:")
            .put("msg.subscribe.vk", "Ну и подписывайся на нас в ВК. У нас тут всегда интересно!")
            .put("msg.correct.answer", "Ура, правильно!")
            .put("msg.wrong.answer", "Эх, неправильно. Напиши СТОП, если сдаешься или попробуй еще раз!")
            .put("msg.win", "Ура, челлендж пройден! Теперь тебе доступны новые стикерпаки от наших друзей ВКонтакте! Срочно проверь https://vk.com/stickers")
            .put("msg.already.winner", "Теперь нужно дождаться следующего выпуска на нашем канале. Ставь колокольчик на нашем YouTube канале vk.cc/9qhYki, чтобы успеть первым. Будут новые вопросы и новые стикеры!")
            .put("msg.come.later", "Ок, возвращайся потом")
            .put("msg.limit", "Ой, уже все разобрали((( Ставь колокольчик на нашем YouTube канале vk.cc/9qhYki, чтобы в следующий раз успеть первым!")
            .put("msg.buy", "Ставь колокольчик на нашем YouTube канале vk.cc/9qhYki, чтобы всегда успевать первым. С каждым выпуском новые вопросы и новые стикеры!")
            .build();

    @Mock
    private VkApiService vkApiService;

    @Mock
    private DialogChain dialogChain;

    private MessageNewHandler questionAnswerStateMachine;

    private MessageSource messageSource = new TestMessageSource();

    @Before
    public void init() throws Exception {
        reset(vkApiService, dialogChain);
        questionAnswerStateMachine = new MessageNewHandler(new ConcurrentMapQuestionAnswerState(),
                new ConcurrentMapWinnerState(),
                vkApiService,
                dialogChain,
                new Messages(messageSource));
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
        verify(vkApiService).sendMessage(userId, map.get("msg.welcome"));
        verify(vkApiService).sendMessage(userId, questionAnswer.getQuestion());

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
        verify(vkApiService).sendMessage(userId, map.get("msg.welcome"));
        verify(vkApiService).sendMessage(userId, questionAnswer.getQuestion());

    }

    @Test
    public void shouldSendWinMsg_WhenAllAnswersCorrect() throws Exception {
        //arrange
        String msg = "Хочу стикеры";
        Integer userId = new Random().nextInt();

        QuestionAnswer questionAnswer_1 = QuestionAnswer.builder()
                .question("What question_1")
                .addCorrectAnswer("Correct_1")
                .build();

        QuestionAnswer questionAnswer_2 = QuestionAnswer.builder()
                .question("What question_2")
                .addCorrectAnswer("Correct_2")
                .build();

        QuestionAnswerChainElement chainElement = QuestionAnswerChainElement.builder()
                .current(questionAnswer_1)
                .next(QuestionAnswerChainElement.builder()
                        .current(questionAnswer_2)
                        .build())
                .build();

        when(dialogChain.getFirst()).thenReturn(chainElement);

        //act
        questionAnswerStateMachine.handle(mockMsg(userId, msg));
        questionAnswerStateMachine.handle(mockMsg(userId, "Correct_1"));

        verify(vkApiService, times(4)).sendMessage(anyInt(), anyString());

        questionAnswerStateMachine.handle(mockMsg(userId, "Correct_2"));

        //assert
        verify(vkApiService).sendMessage(userId, map.get("msg.win"));
    }

    @Test
    public void shouldSendTryAgain_WhenSecondWrongAnswer() throws Exception {
        //arrange
        String msg = "Хочу стикеры";
        Integer userId = new Random().nextInt();

        QuestionAnswer questionAnswer_1 = QuestionAnswer.builder()
                .question("What question_1")
                .addCorrectAnswer("Correct_1")
                .build();

        QuestionAnswer questionAnswer_2 = QuestionAnswer.builder()
                .question("What question_2")
                .addCorrectAnswer("Correct_2")
                .build();

        QuestionAnswerChainElement chainElement = QuestionAnswerChainElement.builder()
                .current(questionAnswer_1)
                .next(QuestionAnswerChainElement.builder()
                        .current(questionAnswer_2)
                        .build())
                .build();

        when(dialogChain.getFirst()).thenReturn(chainElement);

        //act
        questionAnswerStateMachine.handle(mockMsg(userId, msg));
        questionAnswerStateMachine.handle(mockMsg(userId, "Correct_1"));
        verify(vkApiService, times(4)).sendMessage(anyInt(), anyString());


        questionAnswerStateMachine.handle(mockMsg(userId, "Wrong_2"));

        //assert

        verify(vkApiService).sendMessage(userId, map.get("msg.wrong.answer"));
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
                new Messages(messageSource));

        //act
        questionAnswerStateMachine.handle(mockMsg(userId, msg));

        //assert
        verify(vkApiService, times(0)).sendMessage(userId, map.get("msg.wrong.answer"));

        verify(vkApiService, times(1)).sendMessage(userId, map.get("msg.already.winner"));

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
        verify(vkApiService).sendMessage(userId, map.get("msg.wrong.answer"));

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
        verify(vkApiService).sendMessage(userId, map.get("msg.win"));

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
        verify(vkApiService).sendMessage(userId, map.get("msg.subscribe.vk"));
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
        verify(vkApiService).sendMessage(userId, map.get("msg.win"));

    }

    private static class TestMessageSource implements MessageSource {

        @Override
        public String getMessage(String code, @Nullable Object[] args, @Nullable String defaultMessage, Locale locale) {
            return map.get(code);
        }

        @Override
        public String getMessage(String code, @Nullable Object[] args, Locale locale) throws NoSuchMessageException {
            return map.get(code);
        }

        @Override
        public String getMessage(MessageSourceResolvable resolvable, Locale locale) throws NoSuchMessageException {
            return null;
        }
    }

}