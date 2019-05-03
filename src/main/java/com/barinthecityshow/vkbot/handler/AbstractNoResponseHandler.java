package com.barinthecityshow.vkbot.handler;

import com.vk.api.sdk.callback.objects.messages.CallbackMessageBase;

public abstract class AbstractNoResponseHandler implements CallbackMessageHandler {
    private static final String OK_BODY = "ok";

    @Override
    public String handle(CallbackMessageBase message) {
        doHandle(message);
        return OK_BODY;
    }

    protected abstract void doHandle(CallbackMessageBase message);
}
