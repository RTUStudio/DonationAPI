package kr.rtustudio.donation.bukkit.configuration.service;

import kr.rtustudio.configurate.objectmapping.meta.Comment;
import kr.rtustudio.donation.bukkit.platform.ServiceBuilder;
import kr.rtustudio.donation.common.configuration.SocketOption;
import kr.rtustudio.framework.bukkit.api.configuration.ConfigurationPart;
import lombok.Getter;

@Getter
@SuppressWarnings({"unused", "FieldMayBeFinal", "InnerClassMayBeStatic"})
public class ChzzkConfig extends ConfigurationPart implements kr.rtustudio.donation.service.chzzk.configuration.ChzzkConfig, ServiceBuilder.EnabledConfig {

    @Comment("서비스 활성화 여부")
    private boolean enabled = true;

    @Comment("클라이언트 ID")
    private String clientId = "00000000-0000-0000-0000-000000000000";

    @Comment("클라이언트 시크릿")
    private String clientSecret = "secret";

    @Comment("접속용 주소")
    private String baseUri = "http://localhost:12345";

    @Comment("호스트")
    private String host = "0.0.0.0";

    @Comment("포트")
    private int port = 20260;

    @Comment("소켓 옵션")
    private Socket socket = new Socket();

    @Getter
    public class Socket extends ConfigurationPart implements SocketOption {
        @Comment("소켓 연결 타임아웃 (ms)")
        private int timeout = 3000;

        @Comment("Keepalive 설정 섹션")
        private Keepalive keepalive = new Keepalive();
        @Comment("재연결 설정 섹션")
        private Reconnection reconnection = new Reconnection();

        @Override
        public boolean isKeepaliveEnabled() {
            return keepalive != null && keepalive.enabled;
        }

        @Override
        public int getKeepaliveInterval() {
            return keepalive != null ? keepalive.interval : 60000;
        }

        @Override
        public boolean isReconnectionEnabled() {
            return reconnection != null && reconnection.enabled;
        }

        @Override
        public int getReconnectionDelay() {
            return reconnection != null ? reconnection.delay : 1000;
        }

        @Override
        public int getReconnectionMaxDelay() {
            return reconnection != null ? reconnection.maxDelay : 3000;
        }

        @Getter
        public class Keepalive extends ConfigurationPart {
            @Comment("서버-클라이언트 간 keepalive(ping) 사용 여부")
            private boolean enabled = false;

            @Comment("keepalive 주기 (ms)")
            private int interval = 60000;
        }

        @Getter
        public class Reconnection extends ConfigurationPart {
            @Comment("자동 재연결 활성화 여부")
            private boolean enabled = false;

            @Comment("재연결 최초 지연 (ms)")
            private int delay = 1000;

            @Comment("재연결 최대 지연 (ms)")
            private int maxDelay = 3000;
        }
    }
}
