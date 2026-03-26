package kr.rtustudio.donation.common;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.Nullable;

public enum Platform {
    CHZZK,
    SOOP,
    YOUTUBE,
    TOONATION,
    CIME;

    private static final Object2ObjectMap<String, Platform> INDEX = new Object2ObjectOpenHashMap<>();

    static {
        for (Platform type : values()) INDEX.put(type.name(), type);
    }

    @Nullable
    public static Platform from(String name) {
        return INDEX.get(name);
    }
}
