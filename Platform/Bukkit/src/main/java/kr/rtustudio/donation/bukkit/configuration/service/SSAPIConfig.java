package kr.rtustudio.donation.bukkit.configuration.service;

import kr.rtustudio.configurate.objectmapping.meta.Comment;
import kr.rtustudio.donation.bukkit.platform.ConfigurableDonationPlatform;
import kr.rtustudio.donation.common.configuration.ServiceConfig;
import kr.rtustudio.framework.bukkit.api.configuration.ConfigurationPart;
import lombok.Getter;

@Getter
@SuppressWarnings({"unused", "FieldMayBeFinal", "InnerClassMayBeStatic"})
public class SSAPIConfig extends ConfigurationPart implements kr.rtustudio.donation.service.ssapi.configuration.SSAPIConfig, ConfigurableDonationPlatform.EnabledConfig {

    @Comment("서비스 활성화 여부")
    private boolean enabled = true;

    @Comment("SSAPI API 키")
    private String apiKey = "";

    @Comment("소켓 옵션")
    private Socket socket = new Socket();

    @Comment("로그인 재시도 간격 (ms)")
    private int loginRetryDelay = 1000;

    @Getter
    public class Socket extends ConfigurationPart implements ServiceConfig.Socket {
        @Comment("소켓 연결 타임아웃 (ms)")
        private int timeout = 3000;

        @Comment("Keepalive 설정 섹션")
        private Socket.Keepalive keepalive = new Socket.Keepalive();
        @Comment("재연결 설정 섹션")
        private Socket.Reconnection reconnection = new Socket.Reconnection();

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