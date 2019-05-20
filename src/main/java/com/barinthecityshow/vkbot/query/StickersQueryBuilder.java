package com.barinthecityshow.vkbot.query;

import com.vk.api.sdk.client.AbstractQueryBuilder;
import com.vk.api.sdk.client.VkApiClient;

import java.util.Collection;
import java.util.Collections;

public class StickersQueryBuilder extends AbstractQueryBuilder<StickersQueryBuilder, Integer> {

    public StickersQueryBuilder(VkApiClient client, String accessToken) {
        super(client, "gifts.addPromoGift", Integer.class);
        accessToken(accessToken);
    }

    public StickersQueryBuilder userId(Integer value) {
        return unsafeParam("user_id", value);
    }

    public StickersQueryBuilder giftId(Integer value) {
        return unsafeParam("gift_id", value);
    }

    public StickersQueryBuilder ownerId(Integer value) {
        return unsafeParam("owner_id", value);
    }

    public StickersQueryBuilder text(String value) {
        return unsafeParam("text", value);
    }

    public StickersQueryBuilder randomId(Integer value) {
        return unsafeParam("random_id", value);
    }

    @Override
    protected StickersQueryBuilder getThis() {
        return this;
    }

    @Override
    protected Collection<String> essentialKeys() {
        return Collections.singletonList("access_token");
    }
}
