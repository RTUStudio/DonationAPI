package kr.rtustudio.donation.bukkit.configuration.service;

import kr.rtustudio.configurate.objectmapping.meta.Comment;
import kr.rtustudio.donation.bukkit.platform.ServiceBuilder;
import kr.rtustudio.framework.bukkit.api.configuration.ConfigurationPart;
import lombok.Getter;

@Getter
@SuppressWarnings({"unused", "FieldMayBeFinal"})
public class SOOPConfig extends ConfigurationPart implements kr.rtustudio.donation.service.soop.configuration.SOOPConfig, ServiceBuilder.EnabledConfig {

    @Comment("서비스 활성화 여부")
    private boolean enabled = true;

    @Comment("클라이언트 ID")
    private String clientId = "00000000000000000000000000000000";

    @Comment("클라이언트 시크릿")
    private String clientSecret = "secret";

    @Comment("접속용 주소")
    private String baseUri = "http://localhost:12346";

    @Comment("호스트")
    private String host = "0.0.0.0";

    @Comment("포트")
    private int port = 20261;
}
