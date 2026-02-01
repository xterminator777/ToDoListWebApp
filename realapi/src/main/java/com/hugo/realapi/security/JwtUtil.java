package com.hugo.realapi.security;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

public class JwtUtil {
    private final byte[] secret;
    private final long expSeconds;

    public JwtUtil(String secret, long expMinutes) {
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        this.expSeconds = expMinutes * 60L;
    }

    public String createToken(Long userId, String email) {
        long now = Instant.now().getEpochSecond();
        long exp = now + expSeconds;

        String headerJson = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
        String payloadJson = "{\"sub\":\"" + userId + "\",\"email\":\"" + escape(email) + "\",\"iat\":" + now + ",\"exp\":" + exp + "}";

        String header = b64Url(headerJson.getBytes(StandardCharsets.UTF_8));
        String payload = b64Url(payloadJson.getBytes(StandardCharsets.UTF_8));
        String signingInput = header + "." + payload;
        String sig = hmacSha256(signingInput);

        return signingInput + "." + sig;
    }

    public JwtClaims verifyAndParse(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 3) throw new RuntimeException("Invalid token");

        String signingInput = parts[0] + "." + parts[1];
        String expected = hmacSha256(signingInput);
        if (!constantTimeEquals(expected, parts[2])) throw new RuntimeException("Bad signature");

        String payloadJson = new String(b64UrlDecode(parts[1]), StandardCharsets.UTF_8);

        long exp = extractLong(payloadJson, "\"exp\":");
        if (Instant.now().getEpochSecond() > exp) throw new RuntimeException("Token expired");

        long userId = Long.parseLong(extractString(payloadJson, "\"sub\":\"", "\""));
        String email = extractString(payloadJson, "\"email\":\"", "\"");

        return new JwtClaims(userId, email, exp);
    }

    public record JwtClaims(long userId, String email, long exp) {}

    private String hmacSha256(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            return b64Url(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new RuntimeException("HMAC failed", e);
        }
    }

    private static String b64Url(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
    private static byte[] b64UrlDecode(String s) {
        return Base64.getUrlDecoder().decode(s);
    }

    private static boolean constantTimeEquals(String a, String b) {
        if (a.length() != b.length()) return false;
        int r = 0;
        for (int i = 0; i < a.length(); i++) r |= a.charAt(i) ^ b.charAt(i);
        return r == 0;
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String extractString(String json, String start, String endDelim) {
        int i = json.indexOf(start);
        if (i < 0) throw new RuntimeException("Missing claim");
        i += start.length();
        int j = json.indexOf(endDelim, i);
        if (j < 0) throw new RuntimeException("Bad claim");
        return json.substring(i, j);
    }

    private static long extractLong(String json, String start) {
        int i = json.indexOf(start);
        if (i < 0) throw new RuntimeException("Missing claim");
        i += start.length();
        int j = i;
        while (j < json.length() && Character.isDigit(json.charAt(j))) j++;
        return Long.parseLong(json.substring(i, j));
    }
}

