package kr.rtustudio.donation.bukkit.configuration.service;

import kr.rtustudio.configurate.objectmapping.meta.Comment;
import kr.rtustudio.donation.bukkit.platform.ServiceBuilder;
import kr.rtustudio.donation.common.configuration.SocketOption;
import kr.rtustudio.configurate.model.ConfigurationPart;
import lombok.Getter;

@Getter
@SuppressWarnings({"unused", "CanBeFinal", "FieldCanBeLocal", "FieldMayBeFinal", "InnerClassMayBeStatic"})
public class SSAPIConfig extends ConfigurationPart implements kr.rtustudio.donation.service.ssapi.configuration.SSAPIConfig, ServiceBuilder.EnabledConfig {

    @Comment("서비스 활성화 여부")
    private boolean enabled = true;

    @Comment("브랜드 색상 (HEX)")
    private String color = "#FFFFFF";

    @Comment("SSAPI API 키")
    private String apiKey = "";

    @Comment("소켓 옵션")
    private Socket socket;

    @Comment("로그인 재시도 간격 (ms)")
    private int loginRetryDelay = 1000;

    @Getter
    public class Socket extends ConfigurationPart implements SocketOption {
        @Comment("소켓 연결 타임아웃 (ms)")
        private int timeout = 3000;

        @Comment("Keepalive 설정 섹션")
        private Keepalive keepalive;
        @Comment("재연결 설정 섹션")
        private Reconnection reconnection;



        @Getter
        public class Keepalive extends ConfigurationPart implements KeepaliveOption {
            @Comment("서버-클라이언트 간 keepalive(ping) 사용 여부")
            private boolean enabled = true;

            @Comment("keepalive 주기 (ms)")
            private int interval = 60000;
        }

        @Getter
        public class Reconnection extends ConfigurationPart implements ReconnectionOption {
            @Comment("자동 재연결 활성화 여부")
            private boolean enabled = true;

            @Comment("재연결 최초 지연 (ms)")
            private int delay = 1000;

            @Comment("재연결 최대 지연 (ms)")
            private int maxDelay = 3000;
        }
    }
}