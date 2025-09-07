package co.com.crediya.autenticacion.api.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.proc.SecurityContext;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class JwtService {

    // ====== Propiedades ======
    @Value("${app.jwt.secret}")
    private String secret; // Base64 o texto plano

    @Value("${app.jwt.issuer:crediya-auth}")
    private String issuer;

    @Value("${app.jwt.access-minutes:30}")
    private long accessMinutes;

    @Value("${app.jwt.refresh-days:7}")
    private long refreshDays;

    // ====== Encoder/Decoder ======
    private JwtEncoder encoder;
    private JwtDecoder decoder;

    @PostConstruct
    void init() {
        byte[] secretBytes = decodeSecret(secret);
        if (secretBytes.length < 32) {
            throw new IllegalArgumentException("app.jwt.secret debe tener >= 32 bytes (256 bits) para HS256");
        }
        // Encoder con JWKSource (Nimbus)
        this.encoder = new NimbusJwtEncoder(new ImmutableSecret<SecurityContext>(secretBytes));

        // Decoder con SecretKey (Nimbus)
        SecretKey key = new SecretKeySpec(secretBytes, "HmacSHA256");
        this.decoder = NimbusJwtDecoder.withSecretKey(key)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }

    // ===================== API =====================

    /** Emite access + refresh en una sola llamada. */
    public Mono<Tokens> issue(String subject, String role, List<String> scopes) {
        return Mono.fromSupplier(() -> {
            Instant now = Instant.now();
            String access = createAccessToken(subject, role, scopes, now);
            String refresh = createRefreshToken(subject, now);
            long expiresIn = accessMinutes * 60; // segundos
            return new Tokens("Bearer", access, refresh, expiresIn);
        });
    }

    /** Genera solo access token. */
    public Mono<String> generateAccess(String subject, String role, List<String> scopes) {
        return Mono.fromSupplier(() -> createAccessToken(subject, role, scopes, Instant.now()));
    }

    /** Genera solo refresh token. */
    public Mono<String> generateRefresh(String subject) {
        return Mono.fromSupplier(() -> createRefreshToken(subject, Instant.now()));
    }

    /** Renueva un access token a partir de un refresh válido (typ=refresh). */
    public Mono<String> refreshAccess(String refreshToken, String role, List<String> scopes) {
        return parseAndValidate(refreshToken)
                .flatMap(claims -> {
                    Object typ = claims.get("typ");
                    if (!"refresh".equalsIgnoreCase(typ == null ? "" : typ.toString())) {
                        return Mono.error(new IllegalArgumentException("El token provisto no es un refresh token"));
                    }
                    String subject = (String) claims.get("sub"); // subject según spec (sub)
                    return generateAccess(subject, role, scopes);
                });
    }

    /** Decodifica y valida un JWT. Retorna el mapa de claims. */
    public Mono<Map<String, Object>> parseAndValidate(String token) {
        return Mono.fromSupplier(() -> this.decoder.decode(token).getClaims());
    }

    /** Indica si el token es de tipo refresh. */
    public Mono<Boolean> isRefreshToken(String token) {
        return parseAndValidate(token)
                .map(c -> "refresh".equalsIgnoreCase(String.valueOf(c.get("typ"))));
    }

    // ===================== Internos =====================

    private String createAccessToken(String subject, String role, List<String> scopes, Instant now) {
        String scopeStr = String.join(" ", scopes == null ? List.of() : scopes);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .subject(subject)
                .issuedAt(now)
                .expiresAt(now.plus(accessMinutes, ChronoUnit.MINUTES))
                .id(UUID.randomUUID().toString())   // jti
                .claim("role", role)
                .claim("scope", scopeStr)
                .build();

        JwsHeader jws = JwsHeader.with(MacAlgorithm.HS256).build();
        return this.encoder.encode(JwtEncoderParameters.from(jws, claims)).getTokenValue();
    }

    private String createRefreshToken(String subject, Instant now) {
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .subject(subject)
                .issuedAt(now)
                .expiresAt(now.plus(refreshDays, ChronoUnit.DAYS))
                .id(UUID.randomUUID().toString())
                .claim("typ", "refresh")
                .build();

        JwsHeader jws = JwsHeader.with(MacAlgorithm.HS256).build();
        return this.encoder.encode(JwtEncoderParameters.from(jws, claims)).getTokenValue();
    }

    private byte[] decodeSecret(String value) {
        try {
            return Base64.getDecoder().decode(value);
        } catch (IllegalArgumentException e) {
            return value.getBytes(StandardCharsets.UTF_8);
        }
    }

    // DTO simple para /login
    public record Tokens(String tokenType, String accessToken, String refreshToken, long expiresIn) {}
}
