package com.barinthecityshow.vkbot.handler;

import com.vk.api.sdk.callback.objects.messages.CallbackMessageBase;
import com.vk.api.sdk.callback.objects.messages.CallbackMessageType;

public interface CallbackMessageHandler {
    String handle(CallbackMessageBase message);

    CallbackMessageType getType();

}
