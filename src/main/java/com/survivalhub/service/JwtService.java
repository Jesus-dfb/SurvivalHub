package com.survivalhub.service;

import com.survivalhub.model.AppUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class JwtService {

    private final String secret;
    private final long expirationSeconds;

    public JwtService(
            @Value("${app.jwt.secret:survival-hub-local-dev-secret-change-me}") String secret,
            @Value("${app.jwt.expiration-seconds:86400}") long expirationSeconds
    ) {
        this.secret = secret;
        this.expirationSeconds = expirationSeconds;
    }

    public String createToken(AppUser user) {
        String header = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
        long expiration = Instant.now().getEpochSecond() + expirationSeconds;
        String payload = "{"
                + "\"sub\":\"" + escapeJson(user.getUsername()) + "\","
                + "\"userId\":" + user.getId() + ","
                + "\"displayName\":\"" + escapeJson(user.getDisplayName()) + "\","
                + "\"exp\":" + expiration
                + "}";

        String headerPart = encodeJson(header);
        String payloadPart = encodeJson(payload);
        String signaturePart = sign(headerPart + "." + payloadPart);

        return headerPart + "." + payloadPart + "." + signaturePart;
    }

    public Optional<String> validateTokenAndGetUsername(String token) {
        try {
            String[] parts = token.split("\\.");

            if (parts.length != 3) {
                return Optional.empty();
            }

            String expectedSignature = sign(parts[0] + "." + parts[1]);

            if (!constantTimeEquals(expectedSignature, parts[2])) {
                return Optional.empty();
            }

            String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            long expiration = extractLong(payload, "exp");

            if (Instant.now().getEpochSecond() >= expiration) {
                return Optional.empty();
            }

            String subject = extractString(payload, "sub");

            if (subject.isBlank()) {
                return Optional.empty();
            }

            return Optional.of(subject);
        } catch (Exception exception) {
            return Optional.empty();
        }
    }

    private String encodeJson(String json) {
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(json.getBytes(StandardCharsets.UTF_8));
    }

    private String sign(String content) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(key);
            byte[] signature = mac.doFinal(content.getBytes(StandardCharsets.UTF_8));

            return Base64.getUrlEncoder().withoutPadding().encodeToString(signature);
        } catch (Exception exception) {
            throw new IllegalStateException("No se pudo firmar el token", exception);
        }
    }

    private boolean constantTimeEquals(String first, String second) {
        byte[] firstBytes = first.getBytes(StandardCharsets.UTF_8);
        byte[] secondBytes = second.getBytes(StandardCharsets.UTF_8);

        if (firstBytes.length != secondBytes.length) {
            return false;
        }

        int result = 0;

        for (int index = 0; index < firstBytes.length; index++) {
            result |= firstBytes[index] ^ secondBytes[index];
        }

        return result == 0;
    }

    private String escapeJson(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private String extractString(String json, String key) {
        Pattern pattern = Pattern.compile("\"" + key + "\"\\s*:\\s*\"([^\"]*)\"");
        Matcher matcher = pattern.matcher(json);

        if (!matcher.find()) {
            return "";
        }

        return matcher.group(1).replace("\\\"", "\"").replace("\\\\", "\\");
    }

    private long extractLong(String json, String key) {
        Pattern pattern = Pattern.compile("\"" + key + "\"\\s*:\\s*(\\d+)");
        Matcher matcher = pattern.matcher(json);

        if (!matcher.find()) {
            return 0;
        }

        return Long.parseLong(matcher.group(1));
    }
}
