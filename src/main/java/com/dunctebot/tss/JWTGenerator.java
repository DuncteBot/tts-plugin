package com.dunctebot.tss;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.*;

import static com.dunctebot.tss.TTSAudioSourceManager.GOOGLE_API_URL;

public class JWTGenerator {
    private final String clientEmail;
    private final String privateKey;

    private Date expiresAt = new Date();
    private String jwtCache = null;

    public JWTGenerator(String clientEmail, String privateKey) {
        this.clientEmail = clientEmail;
        this.privateKey = privateKey;
    }

    public String getJWT() {
        if (this.jwtCache == null || this.expiresAt.after(new Date())) {
            this.jwtCache = this.generateToken();
        }

        return this.jwtCache;
    }

    private Date getExpiry() {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.HOUR_OF_DAY, 1);

        return calendar.getTime();
    }

    private String generateToken() {
        this.expiresAt = getExpiry();

        return Jwts.builder()
                // claims
                .setAudience(GOOGLE_API_URL)   // aud
                .setIssuer(this.clientEmail)   // iss
                .setSubject(this.clientEmail)  // sub
                .setIssuedAt(new Date())       // iat
                .setExpiration(this.expiresAt) // exp
                // additional headers
                .setHeaderParam("kid", this.privateKey)
                .signWith(SignatureAlgorithm.RS256, Base64.getEncoder().encodeToString(this.privateKey.getBytes()))
                .compact();
    }
}
