package kr.rtustudio.donation.service.soop.configuration;

public interface SOOPConfig {

    boolean isEnabled();

    String getClientId();

    String getClientSecret();

    String getBaseUri();

    String getHost();

    int getPort();
}
