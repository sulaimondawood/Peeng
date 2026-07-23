package com.dawood.peeng.security;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class JwtService {

    @Value("${app.security.jwt}")
    private String secretKey;

    private Algorithm algorithm() {
        return Algorithm.HMAC256(secretKey);
    }

    public String generateToken(Map<String, String> claims, String sub) {

        var builder = JWT.create()
                .withIssuer("Peeng")
                .withSubject(sub)
                .withIssuedAt(Instant.now())
                .withExpiresAt(Instant.now().plus(3, ChronoUnit.DAYS));

        if (claims != null) {
            claims.forEach((k, v) -> {
                builder.withClaim(k, v);
            });
        }

        return builder.sign(algorithm());

    }

    public DecodedJWT verifyToken(String token) {

        try {
            JWTVerifier verifier = JWT.require(algorithm())
                    .withIssuer("Peeng")
                    .build();

            return verifier.verify(token);
        } catch (JWTVerificationException exception) {
            log.warn("JWT verification failed: {}", exception.getMessage());
            throw exception;
        }
    }

    public String extractSubject(String token) {
        return verifyToken(token)
                .getSubject();
    }

}
