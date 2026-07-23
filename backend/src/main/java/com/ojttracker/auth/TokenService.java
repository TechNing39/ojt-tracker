package com.ojttracker.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Optional;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TokenService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder DECODER = Base64.getUrlDecoder();

    private record Payload(String role, Long siteId, long exp) {
    }

    private final ObjectMapper objectMapper;
    private final String secret;
    private final long ttlDays;

    public TokenService(
            ObjectMapper objectMapper,
            @Value("${app.auth.token-secret}") String secret,
            @Value("${app.auth.token-ttl-days}") long ttlDays) {
        this.objectMapper = objectMapper;
        this.secret = secret;
        this.ttlDays = ttlDays;
    }

    public String issueToken(Role role, Long siteId) {
        long exp = Instant.now().plus(ttlDays, ChronoUnit.DAYS).getEpochSecond();
        Payload payload = new Payload(role.name(), siteId, exp);
        try {
            String payloadJson = objectMapper.writeValueAsString(payload);
            String encodedPayload = ENCODER.encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8));
            return encodedPayload + "." + sign(encodedPayload);
        } catch (Exception e) {
            throw new IllegalStateException("토큰 발급 실패", e);
        }
    }

    public Optional<TokenPrincipal> verify(String token) {
        try {
            int dotIndex = token.lastIndexOf('.');
            if (dotIndex < 0) {
                return Optional.empty();
            }
            String encodedPayload = token.substring(0, dotIndex);
            String signature = token.substring(dotIndex + 1);

            if (!constantTimeEquals(sign(encodedPayload), signature)) {
                return Optional.empty();
            }

            String payloadJson = new String(DECODER.decode(encodedPayload), StandardCharsets.UTF_8);
            Payload payload = objectMapper.readValue(payloadJson, Payload.class);

            Instant exp = Instant.ofEpochSecond(payload.exp());
            if (exp.isBefore(Instant.now())) {
                return Optional.empty();
            }

            return Optional.of(new TokenPrincipal(Role.valueOf(payload.role()), payload.siteId(), exp));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private String sign(String data) throws Exception {
        Mac mac = Mac.getInstance(HMAC_ALGORITHM);
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
        return ENCODER.encodeToString(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
    }

    private boolean constantTimeEquals(String a, String b) {
        return MessageDigest.isEqual(a.getBytes(StandardCharsets.UTF_8), b.getBytes(StandardCharsets.UTF_8));
    }
}
