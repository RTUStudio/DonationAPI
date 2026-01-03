package kr.rtustudio.donation.bukkit.configuration.service;

import kr.rtustudio.configurate.objectmapping.meta.Comment;
import kr.rtustudio.donation.common.configuration.ServiceConfig;
import kr.rtustudio.framework.bukkit.api.configuration.ConfigurationPart;
import lombok.Getter;

@Getter
@SuppressWarnings({"unused", "FieldMayBeFinal", "InnerClassMayBeStatic"})
public class ChzzkConfig extends ConfigurationPart implements kr.rtustudio.donation.service.chzzk.configuration.ChzzkConfig {

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
    private int port = 12345;

    @Comment("소켓 옵션")
    private Socket socket = new Socket();

    @Getter
    public class Socket extends ConfigurationPart implements ServiceConfig.Socket {
        @Comment("소켓 연결 타임아웃 (ms)")
        private int timeout = 3000;

        @Comment("로그인 재시도 간격 (ms)")
        private int loginRetryDelay = 1000;
        @Comment("Keepalive 설정 섹션")
        private Keepalive keepalive = new Keepalive();
        @Comment("재연결 설정 섹션")
        private Reconnection reconnection = new Reconnection();

        @Override
        public boolean isKeepaliveEnabled() {
            if (keepalive == null) return false;
            return keepalive.enabled;
        }

        @Override
        public int getKeepaliveInterval() {
            if (keepalive == null) return 60000;
            return keepalive.interval;
        }

        @Override
        public boolean isReconnectionEnabled() {
            if (reconnection == null) return false;
            return reconnection.enabled;
        }

        @Override
        public int getReconnectionDelay() {
            if (reconnection == null) return 1000;
            return reconnection.delay;
        }

        @Override
        public int getReconnectionMaxDelay() {
            if (reconnection == null) return 3000;
            return reconnection.maxDelay;
        }

        @Getter
        public class Keepalive extends ConfigurationPart {
            @Comment("서버-클라이언트 간 keepalive(ping) 사용 여부")
            private boolean enabled = true;

            @Comment("keepalive 주기 (ms)")
            private int interval = 60000;
        }

        @Getter
        public class Reconnection extends ConfigurationPart {
            @Comment("자동 재연결 활성화 여부")
            private boolean enabled = true;

            @Comment("재연결 최초 지연 (ms)")
            private int delay = 1000;

            @Comment("재연결 최대 지연 (ms)")
            private int maxDelay = 3000;
        }
    }
}
