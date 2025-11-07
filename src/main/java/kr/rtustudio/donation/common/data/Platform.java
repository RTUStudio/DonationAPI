package kr.rtustudio.donation.common.data;

public enum Platform {

    CHZZK,
    SOOP;

    public String lowercase() {
        return name().toLowerCase();
    }

}
