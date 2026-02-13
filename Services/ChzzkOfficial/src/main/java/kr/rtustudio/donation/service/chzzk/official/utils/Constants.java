package kr.rtustudio.donation.service.chzzk.official.utils;

import com.google.gson.Gson;
import okhttp3.MediaType;
import org.jetbrains.annotations.NotNull;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Constants {

    public static final @NotNull String OPENAPI_URL = "https://openapi.chzzk.naver.com";

    public static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");

    public static final @NotNull Gson GSON = new Gson();

    public static final @NotNull ExecutorService EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();

    public static final @NotNull ZoneId ZONE_ID = ZoneId.of("Asia/Seoul");

    public static final @NotNull DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

}
