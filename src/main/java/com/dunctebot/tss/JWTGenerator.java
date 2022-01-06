package com.dunctebot.tss;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.*;

import static com.dunctebot.tss.TTSAudioSourceManager.GOOGLE_API_URL;

public class JWTGenerator {
    private final String clientEmail;
    private final String privateKeyRaw;
    private final PrivateKey privateKey;

    private Date expiresAt = new Date();
    private String jwtCache = null;

    public JWTGenerator(String clientEmail, String privateKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        final String privateKeyB64 = privateKey
                .replaceAll("\n", "")
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s+","");

        final byte[] privateKeyDecoded = Base64.getDecoder().decode(privateKeyB64);
        final PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyDecoded);
        final KeyFactory kf = KeyFactory.getInstance("RSA");

        this.clientEmail = clientEmail;
        this.privateKeyRaw = privateKey;
        this.privateKey = kf.generatePrivate(keySpec);
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
                .setHeaderParam("kid", this.privateKeyRaw)
                .signWith(SignatureAlgorithm.RS256, this.privateKey)
                .compact();
    }
}
