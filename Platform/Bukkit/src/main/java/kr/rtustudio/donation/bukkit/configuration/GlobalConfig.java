package kr.rtustudio.donation.bukkit.configuration;

import kr.rtustudio.configurate.objectmapping.meta.Comment;
import kr.rtustudio.configurate.model.ConfigurationPart;
import kr.rtustudio.donation.common.Platform;
import lombok.Getter;

@Getter
@SuppressWarnings({"unused", "CanBeFinal", "FieldCanBeLocal", "FieldMayBeFinal", "InnerClassMayBeStatic"})
public class GlobalConfig extends ConfigurationPart {

    @Comment("후원 알림 설정")
    private boolean donationNotify = false;

    @Comment("플랫폼별 후원 단위 및 비율(rate) 설정")
    private PlatformSettings platforms;

    @Getter
    public class PlatformSettings extends ConfigurationPart {

        @Comment("치지직 (기본값: 치즈, 1)")
        private PlatformInfo chzzk;

        @Comment("숲 (기본값: 별풍선, 100)")
        private PlatformInfo soop;

        @Comment("유튜브 (기본값: 원, 1)")
        private PlatformInfo youtube;

        @Comment("투네이션 (기본값: 캐시, 1)")
        private PlatformInfo toonation;

        @Comment("씨미 (기본값: 빔, 1)")
        private PlatformInfo cime;

        public PlatformInfo get(Platform platform) {
            return switch (platform) {
                case CHZZK -> chzzk;
                case SOOP -> soop;
                case YOUTUBE -> youtube;
                case TOONATION -> toonation;
                case CIME -> cime;
            };
        }

        @Getter
        public class PlatformInfo extends ConfigurationPart {
            @Comment("후원 단위명")
            private String unit;

            @Comment("1 단위당 원(₩) 환산 가치")
            private int rate;
        }
    }

}
