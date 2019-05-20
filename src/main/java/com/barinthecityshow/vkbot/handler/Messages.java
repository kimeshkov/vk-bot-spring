package com.barinthecityshow.vkbot.handler;

public enum Messages {
    WELCOME_MSG("Привет, раз хочешь стикер, ответь на вопрос: "),
    SUBSCRIBE_MSG("Подпишись и попробуй заново"),
    CORRECT_ANS_MSG("Правильно! Следующий вопрос: "),
    WRONG_ANS_MSG("Эх, неправильно. Напиши СТОП, если сдаешься или попробуй еще раз!"),
    WIN_MSG("Ура, правильно! Теперь тебе доступны новые стикерпаки от наших друзей Вконтакте! Срочно проверь https://vk.com/stickers"),
    BYE_MSG("Ок, возвращайся потом"),
    START_MSG("Хочу стикер"),
    LIMIT_MSG("Ой, уже все разобрали. Приходи после следующего выпуска!"),
    STOP_MSG("Стоп");

    private String value;

    Messages(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
