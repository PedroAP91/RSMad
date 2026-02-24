package com.rsmad.backend.security.jwt;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

@Configuration
public class JwtCryptoConfig {

    @Value("${app.security.jwt.secret}")
    private String jwtSecret;

    @Bean
    public JwtEncoder jwtEncoder() {
        byte[] secretBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return new NimbusJwtEncoder(new ImmutableSecret<>(secretBytes));
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withSecretKey(jwtSecretKey())
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    @Profile("dev")
    public UserDetailsService devUserDetailsService(
            @Value("${spring.security.user.name}") String username,
            @Value("${spring.security.user.password}") String password,
            PasswordEncoder passwordEncoder
    ) {
        String storedPassword = password;
        if (!password.startsWith("{")) {
            storedPassword = passwordEncoder.encode(password);
        }
        UserDetails user = User.withUsername(username)
                .password(storedPassword)
                .roles("USER")
                .build();
        return new InMemoryUserDetailsManager(user);
    }

    private SecretKey jwtSecretKey() {
        byte[] secretBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return new SecretKeySpec(secretBytes, "HmacSHA256");
    }
}
