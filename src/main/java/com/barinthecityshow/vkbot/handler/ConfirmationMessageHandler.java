package com.barinthecityshow.vkbot.handler;

import com.barinthecityshow.vkbot.service.VkApiService;
import com.vk.api.sdk.callback.objects.messages.CallbackMessageBase;
import com.vk.api.sdk.callback.objects.messages.CallbackMessageType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ConfirmationMessageHandler implements CallbackMessageHandler {
    private final VkApiService vkApiService;

    @Autowired
    public ConfirmationMessageHandler(VkApiService vkApiService) {
        this.vkApiService = vkApiService;
    }

    @Override
    public String handle(CallbackMessageBase message) {
        return vkApiService.confirm();
    }

    @Override
    public CallbackMessageType getType() {
        return CallbackMessageType.CONFIRMATION;
    }
}
