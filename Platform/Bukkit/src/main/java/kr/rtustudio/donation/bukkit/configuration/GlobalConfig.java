package kr.rtustudio.donation.bukkit.configuration;

import kr.rtustudio.configurate.objectmapping.meta.Comment;
import kr.rtustudio.framework.bukkit.api.configuration.ConfigurationPart;
import lombok.Getter;

@Getter
@SuppressWarnings({"unused", "FieldMayBeFinal"})
public class GlobalConfig extends ConfigurationPart {

    @Comment("후원 알림 설정")
    private boolean donationNotify = false;

}
