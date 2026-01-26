package ktb.billage.common.cursor;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

@Component
public class CursorCodec {
    private static final String SEPARATOR = "|";

    public String encode(Instant time, long id) {
        String raw = time + SEPARATOR + id;

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    public Cursor decode(String encoded) {
        byte[] decodedBytes = Base64.getUrlDecoder().decode(encoded);
        String raw = new String(decodedBytes, StandardCharsets.UTF_8);
        String[] parts = raw.split("\\|", -1);
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid cursor format");
        }

        Instant time = Instant.parse(parts[0]);
        long id;
        try {
            id = Long.parseLong(parts[1]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid cursor id", e);
        }

        return new Cursor(time, id);
    }

    public record Cursor(Instant time, long id) {
    }
}
