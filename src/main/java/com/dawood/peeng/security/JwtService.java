package com.dawood.peeng.security;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;

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

  private Algorithm algorithm() {
    return Algorithm.HMAC256("78c1133035814e71f2e31e1497707a3c3cfe2472ec5f44909d8ef37051c29bf9");
  }

  public String generateToken(Map<String, String> claims, String sub) {

    var builder = JWT.create()
        .withIssuer("peeng")
        .withSubject(sub)
        .withIssuedAt(Instant.now())
        .withExpiresAt(Instant.now().plus(1, ChronoUnit.DAYS));

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
          .withIssuer("peeng")
          .build();

      return verifier.verify(token);
    } catch (JWTVerificationException exception) {
      log.error("Token verification failed: ", exception);
      return null;
    }
  }

  public String extractSubject(String token) {
    return verifyToken(token)
        .getSubject();
  }

}
