package com.barinthecityshow.vkbot;

import com.barinthecityshow.vkbot.dialog.QuestionAnswer;
import com.barinthecityshow.vkbot.dialog.chain.StickerDialogChain;
import com.barinthecityshow.vkbot.handler.CallbackMessageHandler;
import com.barinthecityshow.vkbot.service.VkApiService;
import com.google.gson.Gson;
import com.vk.api.sdk.callback.objects.messages.CallbackMessageType;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


@Configuration
public class AppConfig {
    private static final Logger LOG = LoggerFactory.getLogger(VkApiService.class);

    @Autowired
    private Environment env;

    @Bean
    public VkApiService vkApiService() {
        HttpTransportClient client = new HttpTransportClient();
        VkApiClient apiClient = new VkApiClient(client);

        GroupActor actor = initVkApi(apiClient);

        return new VkApiService(apiClient, actor, env.getProperty("confirmationCode"));
    }

    private GroupActor initVkApi(VkApiClient apiClient) {
        int groupId = Integer.parseInt(env.getProperty("groupId"));
        String token = env.getProperty("token");
        int serverId = Integer.parseInt(env.getProperty("serverId"));
        if (groupId == 0 || token == null || serverId == 0) throw new RuntimeException("Params are not set");
        GroupActor actor = new GroupActor(groupId, token);

        try {
            apiClient.groups().setCallbackSettings(actor, groupId).serverId(serverId).messageNew(true).execute();
        } catch (ApiException e) {
            throw new RuntimeException("Api error during init", e);
        } catch (ClientException e) {
            throw new RuntimeException("Client error during init", e);
        }

        return actor;
    }

    @Bean
    public StickerDialogChain stickerDialogChain() {
        return initDialogChain();
    }

    private StickerDialogChain initDialogChain() {
        Path path = Paths.get("qa.json");

        LOG.info("Try reading qa from file");
        try {
            String json = Files.readAllLines(path).stream().collect(Collectors.joining());

            Gson gson = new Gson();
            List<QuestionAnswer> questionAnswers = Arrays.stream(gson.fromJson(json, QuestionAnswer[].class))
                    .collect(Collectors.toList());

            return new StickerDialogChain(questionAnswers);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
