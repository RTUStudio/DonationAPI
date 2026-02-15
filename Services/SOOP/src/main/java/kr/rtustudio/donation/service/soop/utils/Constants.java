package kr.rtustudio.donation.service.soop.utils;

import com.google.gson.Gson;
import okhttp3.MediaType;
import org.jetbrains.annotations.NotNull;

public class Constants {

    public static final @NotNull String OPENAPI_URL = "https://openapi.sooplive.co.kr";

    public static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");

    public static final @NotNull Gson GSON = new Gson();

}
