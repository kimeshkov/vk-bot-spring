package com.barinthecityshow.vkbot.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@Component
public class Messages {
    private final MessageSource messageSource;

    @Autowired
    public Messages(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public String getMessage(String code) {
        return messageSource.getMessage(code, new Object[]{}, LocaleContextHolder.getLocale());
    }
}
