package com.barinthecityshow.vkbot.query;

import com.vk.api.sdk.client.AbstractAction;
import com.vk.api.sdk.client.VkApiClient;

public class Stickers extends AbstractAction {

    private final String accessToken;

    public Stickers(VkApiClient vkApiClient, String accessToken) {
        super(vkApiClient);
        this.accessToken = accessToken;
    }

    public StickersQueryBuilder openPromoStickerPack() {
        return new StickersQueryBuilder(getClient(), accessToken);
    }

}
