package kr.rtustudio.donation.bukkit.configuration.service;

import kr.rtustudio.configurate.objectmapping.meta.Comment;
import kr.rtustudio.donation.common.configuration.SocketOption;
import kr.rtustudio.configurate.model.ConfigurationPart;
import lombok.Getter;

@Getter
@SuppressWarnings({"unused", "CanBeFinal", "FieldCanBeLocal", "FieldMayBeFinal", "InnerClassMayBeStatic"})
public class SoopConfig extends ConfigurationPart implements kr.rtustudio.donation.service.soop.configuration.SoopConfig {

    @Comment("서비스 활성화 여부")
    private boolean enabled = true;
    @Comment("브랜드 색상 (HEX)")
    private String color = "#0050FF";
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
    @Comment("라이브 상태 확인 주기 (ms)")
    private long liveCheckInterval = 30000;
    @Comment("소켓 옵션")
    private Socket socket;

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
            private int maxDelay = 30000;
        }
    }
}
