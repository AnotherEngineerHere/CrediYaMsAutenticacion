package co.com.crediya.autenticacion.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.*;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Value("${app.jwt.secret}")
    String secret;

    @Value("${app.jwt.issuer:crediya-auth}")
    String issuer;

    // ---- JWT decoder (reactivo) con HS256 + validación de issuer ----
    @Bean
    public ReactiveJwtDecoder jwtDecoder() {
        byte[] keyBytes = decodeSecret(secret);
        SecretKey key = new SecretKeySpec(keyBytes, "HmacSHA256");
        NimbusReactiveJwtDecoder decoder = NimbusReactiveJwtDecoder.withSecretKey(key)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();

        OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(issuer);
        decoder.setJwtValidator(withIssuer);
        return decoder;
    }

    // ---- Converter: role -> ROLE_ADMIN, scope -> authorities ----
    @Bean
    public Converter<Jwt, ? extends Mono<? extends AbstractAuthenticationToken>> jwtAuthenticationConverter() {
        // Convierte 'scope' (separado por espacios) a authorities (sin prefijo)
        JwtGrantedAuthoritiesConverter scopeConverter = new JwtGrantedAuthoritiesConverter();
        scopeConverter.setAuthoritiesClaimName("scope");
        scopeConverter.setAuthorityPrefix(""); // usa "" o "SCOPE_" según prefieras

        JwtAuthenticationConverter base = new JwtAuthenticationConverter();
        base.setJwtGrantedAuthoritiesConverter(jwt -> {
            Collection<GrantedAuthority> auths = new ArrayList<>(scopeConverter.convert(jwt));

            String role = jwt.getClaimAsString("role");
            if (role != null && !role.isBlank()) {
                auths.add(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase(Locale.ROOT)));
            }
            return auths;
        });

        return new ReactiveJwtAuthenticationConverterAdapter(base);
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http,
                                                         ReactiveJwtDecoder jwtDecoder,
                                                         Converter<Jwt, ? extends Mono<? extends AbstractAuthenticationToken>> jwtAuthConverter) {

        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(ex -> ex
                        .pathMatchers("/api/v1/usuarios/login").permitAll()
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // User registration only for ADMIN or ASESOR roles
                        .pathMatchers(HttpMethod.POST, "/api/v1/usuarios").hasAnyRole("ADMIN", "ASESOR")
                        // User queries for authenticated users (including get by email)
                        .pathMatchers(HttpMethod.GET, "/api/v1/usuarios/**").permitAll()
                        // Other user operations for ADMIN only
                        .pathMatchers(HttpMethod.PUT, "/api/v1/usuarios/**").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.DELETE, "/api/v1/usuarios/**").hasRole("ADMIN")
                        // lo demás, a tu gusto:
                        .anyExchange().permitAll()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtDecoder(jwtDecoder)
                                .jwtAuthenticationConverter(jwtAuthConverter)
                        )
                )
                .build();
    }

    // --- helpers ---
    private static byte[] decodeSecret(String value) {
        try {
            return Base64.getDecoder().decode(value);
        } catch (IllegalArgumentException e) {
            return value.getBytes(StandardCharsets.UTF_8);
        }
    }
@Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}