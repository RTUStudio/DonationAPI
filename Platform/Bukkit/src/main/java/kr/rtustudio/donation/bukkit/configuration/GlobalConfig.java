package kr.rtustudio.donation.bukkit.configuration;

import kr.rtustudio.configurate.objectmapping.meta.Comment;
import kr.rtustudio.configurate.model.ConfigurationPart;
import lombok.Getter;

@Getter
@SuppressWarnings({"unused", "CanBeFinal", "FieldCanBeLocal", "FieldMayBeFinal", "InnerClassMayBeStatic"})
public class GlobalConfig extends ConfigurationPart {

    @Comment("후원 알림 설정")
    private boolean donationNotify = false;
}
