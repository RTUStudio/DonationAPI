package kr.rtustudio.donation.bukkit.configuration.service;

import kr.rtustudio.configurate.model.ConfigurationPart;
import kr.rtustudio.configurate.objectmapping.meta.Comment;
import kr.rtustudio.donation.bukkit.platform.ServiceBuilder;
import lombok.Getter;

@Getter
@SuppressWarnings({"unused", "CanBeFinal", "FieldCanBeLocal", "FieldMayBeFinal", "InnerClassMayBeStatic"})
public class YoutubeConfig extends ConfigurationPart implements kr.rtustudio.donation.service.youtube.configuration.YoutubeConfig, ServiceBuilder.EnabledConfig {

    @Comment("서비스 활성화 여부")
    private boolean enabled = true;

    @Comment("브랜드 색상 (HEX)")
    private String color = "#FF0000";

    @Comment("유튜브 라이브챗 폴링 간격 (ms)")
    private int pollingIntervalMs = 5000;

    @Comment("라이브 상태 확인 주기 (ms)")
    private long liveCheckInterval = 60000;
}
