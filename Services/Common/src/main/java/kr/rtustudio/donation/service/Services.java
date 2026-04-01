package kr.rtustudio.donation.service;

import kr.rtustudio.donation.common.Platform;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum Services {

    SSAPI("SSAPI", Platform.CHZZK, Platform.SOOP),
    CHZZK("CHZZK", Platform.CHZZK),
    SOOP("SOOP", Platform.SOOP),
    Youtube("Youtube", Platform.YOUTUBE),
    Toonation("Toonation", Platform.TOONATION),
    CIME("CIME", Platform.CIME);

    private static final Map<String, Services> INDEX = Stream.of(values())
            .collect(Collectors.toUnmodifiableMap(s -> s.name().toLowerCase(Locale.ROOT), s -> s));

    @Getter
    private final String storage;

    @Getter
    private final List<Platform> platforms;

    Services(String storage, Platform... platforms) {
        this.storage = storage;
        this.platforms = List.of(platforms);
    }

    /**
     * 이름(대소문자 무관)으로 서비스를 조회합니다.
     *
     * @param name 서비스 이름
     * @return 서비스 인스턴스 (없으면 null)
     */
    @Nullable
    public static Services from(String name) {
        if (name == null) return null;
        return INDEX.get(name.toLowerCase(Locale.ROOT));
    }

}
