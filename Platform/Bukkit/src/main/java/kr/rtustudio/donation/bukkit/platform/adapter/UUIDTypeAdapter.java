package kr.rtustudio.donation.bukkit.platform.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.UUID;

/**
 * UUID GSON 직렬화기
 * <p>
 * UUID를 문자열로 직렬화하고 역직렬화합니다.
 */
public class UUIDTypeAdapter extends TypeAdapter<UUID> {

    @Override
    public void write(JsonWriter out, UUID value) throws IOException {
        if (value == null) out.nullValue();
        else out.value(value.toString());
    }

    @Override
    public UUID read(JsonReader in) throws IOException {
        String value = in.nextString();
        return value == null ? null : UUID.fromString(value);
    }
}
