package kr.rtustudio.donation.bukkit.configuration;

import kr.rtustudio.configurate.objectmapping.meta.Comment;
import kr.rtustudio.framework.bukkit.api.configuration.ConfigurationPart;
import lombok.Getter;

@Getter
@SuppressWarnings({
        "FieldCanBeLocal",
        "FieldMayBeFinal",
        "InnerClassMayBeStatic"
})
public class GlobalConfig extends ConfigurationPart {

    @Comment("SSAPI API 키")
    private String apiKey = "";

}
