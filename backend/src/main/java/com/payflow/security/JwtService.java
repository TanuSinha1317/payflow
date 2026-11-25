package com.payflow.security;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.Date;
@Service
public class JwtService {
  private final SecretKey key; private final long expiryMinutes;
  public JwtService(@Value("${payflow.jwt-secret}") String secret,@Value("${payflow.jwt-expiry-minutes}") long expiryMinutes){this.key=Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));this.expiryMinutes=expiryMinutes;}
  public String issue(String email,String role){Instant now=Instant.now();return Jwts.builder().subject(email).claim("role",role).issuedAt(Date.from(now)).expiration(Date.from(now.plus(Duration.ofMinutes(expiryMinutes)))).signWith(key).compact();}
  public String subject(String token){return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().getSubject();}
}

