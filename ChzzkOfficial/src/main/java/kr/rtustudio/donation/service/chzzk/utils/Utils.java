package kr.rtustudio.donation.service.chzzk.utils;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

public class Utils {

    public static @NotNull ZonedDateTime parseZonedDateTime(@NotNull String date) {
        return LocalDateTime.parse(date, Constants.DATE_TIME_FORMATTER).atZone(Constants.ZONE_ID);
    }

}
