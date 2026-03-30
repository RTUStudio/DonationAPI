package kr.rtustudio.donation.common;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.Nullable;

public enum Platform {
    CHZZK("치즈", 1),
    SOOP("별풍선", 100),
    YOUTUBE("원", 1),
    TOONATION("캐시", 1),
    CIME("빔", 1);

    private static final Object2ObjectMap<String, Platform> INDEX = new Object2ObjectOpenHashMap<>();

    static {
        for (Platform type : values()) INDEX.put(type.name(), type);
    }

    private String unit;
    private int rate;

    Platform(String unit, int rate) {
        this.unit = unit;
        this.rate = rate;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public void setRate(int rate) {
        this.rate = rate;
    }

    /**
     * 플랫폼 후원 단위명을 반환합니다.
     *
     * @return 단위명 (치즈, 별풍선, 원, 캐시, 빔)
     */
    public String unit() {
        return unit;
    }

    /**
     * 1 단위당 원(₩) 환산 비율을 반환합니다.
     *
     * @return 원 환산 비율 (숲 별풍선: 100, 나머지: 1)
     */
    public int rate() {
        return rate;
    }

    @Nullable
    public static Platform from(String name) {
        return INDEX.get(name);
    }
}
