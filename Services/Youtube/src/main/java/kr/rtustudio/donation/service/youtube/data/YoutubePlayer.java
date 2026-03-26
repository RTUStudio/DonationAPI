package kr.rtustudio.donation.service.youtube.data;

import kr.rtustudio.donation.common.Platform;
import kr.rtustudio.donation.service.data.UserData;

import java.util.UUID;

public record YoutubePlayer(UUID uuid, String handle) implements UserData {

    @Override
    public Platform platform() {
        return Platform.YOUTUBE;
    }

    @Override
    public String channelId() {
        return handle;
    }
}
