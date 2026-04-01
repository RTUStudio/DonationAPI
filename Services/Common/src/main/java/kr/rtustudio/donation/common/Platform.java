package kr.rtustudio.donation.common;

import org.jetbrains.annotations.Nullable;

public enum Platform {
    CHZZK("치즈", 1),
    SOOP("별풍선", 100),
    YOUTUBE("원", 1),
    TOONATION("캐시", 1),
    CIME("빔", 1);

    private static final java.util.Map<String, Platform> INDEX = java.util.stream.Stream.of(values())
            .collect(java.util.stream.Collectors.toUnmodifiableMap(Enum::name, p -> p));

    private final String unit;
    private final int rate;

    Platform(String unit, int rate) {
        this.unit = unit;
        this.rate = rate;
    }

    public String unit() {
        return unit;
    }

    public int rate() {
        return rate;
    }

    @Nullable
    public static Platform from(String name) {
        return INDEX.get(name);
    }
}
