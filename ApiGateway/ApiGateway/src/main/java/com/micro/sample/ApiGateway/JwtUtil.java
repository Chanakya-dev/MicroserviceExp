package com.micro.sample.ApiGateway;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.exceptions.JWTVerificationException;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.Date;

@Component
public class JwtUtil {

    // Base64-encoded 256-bit secret key (use your own generated key for production)
    private static final String SECRET_KEY = "yqTtWjhV6f5h7HjkKmCNfFsYQK6vz1S2hUbL6KZHvgw=";

    private static final long EXPIRATION_TIME = 1000 * 60 * 60; // 1 hour

    private final Algorithm algorithm;

    public JwtUtil() {
        byte[] secretBytes = Base64.getDecoder().decode(SECRET_KEY);
        this.algorithm = Algorithm.HMAC256(secretBytes);
        System.out.println("JwtUtil initialized with secret key.");
    }

    // Generate JWT token for username
    public String generateToken(String username) {
        String token = JWT.create()
                .withSubject(username)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .sign(algorithm);
        System.out.println("Generated token for user: " + username);
        return token;
    }

    public String extractUsername(String token) {
        try {
            DecodedJWT decodedJWT = JWT.require(algorithm).build().verify(token);
            String username = decodedJWT.getSubject();
            System.out.println("Extracted username from token: " + username);
            return username;
        } catch (Exception e) {
            System.out.println("Failed to extract username from token: " + e.getMessage());
            return null;
        }
    }

    // Validate token and return username if valid, else null
    public String validateToken(String token) {
        try {
            DecodedJWT decodedJWT = JWT.require(algorithm)
                    .build()
                    .verify(token);
            String username = decodedJWT.getSubject();
            System.out.println("Token validated successfully for user: " + username);
            return username;  // return username if valid
        } catch (JWTVerificationException e) {
            System.out.println("Invalid or expired token: " + e.getMessage());
            return null; // invalid or expired token
        }
    }
}
