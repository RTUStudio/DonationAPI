package kr.rtustudio.donation.common.configuration;

/**
 * 소켓 기반 서비스 설정 인터페이스
 */
public interface SocketServiceConfig extends ServiceConfig {

    SocketOption getSocket();

}
