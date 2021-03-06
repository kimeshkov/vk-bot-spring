package com.barinthecityshow.vkbot.controller;

import com.barinthecityshow.vkbot.handler.CallbackMessageHandler;
import com.barinthecityshow.vkbot.service.VkApiService;
import com.google.gson.Gson;
import com.vk.api.sdk.callback.objects.messages.CallbackMessageBase;
import com.vk.api.sdk.callback.objects.messages.CallbackMessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController("/")
public class VkApiController {
    private static final Logger LOG = LoggerFactory.getLogger(VkApiController.class);

    private final VkApiService vkApiService;
    private Map<CallbackMessageType, CallbackMessageHandler> handlersByType;

    @Autowired
    public VkApiController(VkApiService vkApiService,
                           List<CallbackMessageHandler> callbackMessageHandlers) {
        this.vkApiService = vkApiService;
        this.handlersByType = callbackMessageHandlers.stream()
                .collect(Collectors.toMap(CallbackMessageHandler::getType, Function.identity()));

    }

    @PostMapping
    public String onCallbackMessage(@RequestBody String json) {
        Gson gson = new Gson();
        CallbackMessageBase message = gson.fromJson(json, CallbackMessageBase.class);

        if (!handlersByType.containsKey(message.getType())) {
            LOG.warn("No handlers found for message");
            return null;
        }

        return handlersByType.get(message.getType()).handle(message);

    }


}
