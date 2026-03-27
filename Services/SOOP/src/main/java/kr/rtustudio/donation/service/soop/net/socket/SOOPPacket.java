package kr.rtustudio.donation.service.soop.net.socket;

import okio.ByteString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class SoopPacket {

    public static final byte SEPARATOR = 0x0C;
    private static final byte ESC = 0x1B;
    private static final byte TAB = 0x09;

    public record ParsedPacket(int serviceCode, int retCode, @NotNull String[] fields) {}

    public static @Nullable ParsedPacket parse(@NotNull byte[] data) {
        if (data.length < 14) return null;

        try {
            String header = new String(data, 0, 14, StandardCharsets.US_ASCII);
            int serviceCode = Integer.parseInt(header.substring(2, 6));
            int retCode = Integer.parseInt(header.substring(12, 14));

            List<byte[]> fieldBytes = new ArrayList<>();
            ByteArrayOutputStream current = new ByteArrayOutputStream();
            for (int i = 14; i < data.length; i++) {
                if (data[i] == SEPARATOR) {
                    fieldBytes.add(current.toByteArray());
                    current = new ByteArrayOutputStream();
                } else {
                    current.write(data[i]);
                }
            }
            fieldBytes.add(current.toByteArray());

            String[] fields = new String[fieldBytes.size()];
            for (int i = 0; i < fieldBytes.size(); i++) {
                fields[i] = new String(fieldBytes.get(i), StandardCharsets.UTF_8);
            }

            return new ParsedPacket(serviceCode, retCode, fields);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static @NotNull ByteString encode(int serviceCode, @NotNull String... fields) {
        ByteArrayOutputStream body = new ByteArrayOutputStream();
        for (String field : fields) {
            body.write(SEPARATOR);
            if (field != null && !field.isEmpty()) {
                byte[] bytes = field.getBytes(StandardCharsets.UTF_8);
                body.write(bytes, 0, bytes.length);
            }
        }

        byte[] bodyBytes = body.toByteArray();

        String headerStr = "" + (char) ESC + (char) TAB
                + String.format("%04d", serviceCode)
                + String.format("%06d", bodyBytes.length)
                + String.format("%02d", 0);
        byte[] headerBytes = headerStr.getBytes(StandardCharsets.US_ASCII);

        byte[] packet = new byte[headerBytes.length + bodyBytes.length];
        System.arraycopy(headerBytes, 0, packet, 0, headerBytes.length);
        System.arraycopy(bodyBytes, 0, packet, headerBytes.length, bodyBytes.length);

        return ByteString.of(packet);
    }

}
