package com.barinthecityshow.vkbot.service;

import com.barinthecityshow.vkbot.query.Stickers;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.groups.MemberStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Random;

public class VkApiService {
    private static final Logger LOG = LoggerFactory.getLogger(VkApiService.class);

    private static final Integer RANDOM_GIFT = -1;

    private final VkApiClient apiClient;
    private final GroupActor actor;
    private final String confirmationCode;
    private final String accessToken;

    private final int adminId;

    private final Random random = new Random();

    public VkApiService(VkApiClient apiClient,
                        GroupActor actor,
                        String confirmationCode,
                        String accessToken,
                        int adminId) {
        this.apiClient = apiClient;
        this.actor = actor;
        this.confirmationCode = confirmationCode;
        this.accessToken = accessToken;
        this.adminId = adminId;
    }

    public String confirm() {
        return confirmationCode;
    }

    public void sendMessage(Integer userId, String msg) {
        try {
            apiClient.messages()
                    .send(actor)
                    .message(msg)
                    .userId(userId)
                    .randomId(random.nextInt())
                    .execute();

        } catch (ApiException e) {
            LOG.error("INVALID REQUEST", e);
        } catch (ClientException e) {
            LOG.error("NETWORK ERROR", e);
        }
    }

    public boolean isSubscribed(Integer userId) {
        try {
            return apiClient.groups()
                    .isMemberWithUserIds(actor, String.valueOf(actor.getGroupId()), userId)
                    .execute()
                    .stream()
                    .findFirst()
                    .map(MemberStatus::isMember)
                    .orElse(false);
        } catch (ApiException e) {
            LOG.error("INVALID REQUEST", e);
            throw new RuntimeException(e);
        } catch (ClientException e) {
            LOG.error("NETWORK ERROR", e);
            throw new RuntimeException(e);
        }
    }

    public void openPromoStickerPack(Integer userId) throws ApiException {
        try {
            new Stickers(apiClient, accessToken).openPromoStickerPack()
                    .ownerId(-actor.getGroupId())
                    .giftId(RANDOM_GIFT)
                    .userId(userId)
                    .execute();
        } catch (ClientException e) {
            LOG.error("NETWORK ERROR", e);
            throw new RuntimeException(e);
        }
    }

    public boolean isAdmin(Integer userId) {
        return Objects.equals(adminId, userId);
    }
}

