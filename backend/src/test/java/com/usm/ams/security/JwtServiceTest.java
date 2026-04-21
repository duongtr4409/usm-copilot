package com.usm.ams.security;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private static String pemFromPublicKey(RSAPublicKey pub) throws Exception {
        byte[] encoded = pub.getEncoded();
        String b64 = java.util.Base64.getEncoder().encodeToString(encoded);
        return "-----BEGIN PUBLIC KEY-----\n" + b64 + "\n-----END PUBLIC KEY-----";
    }

    @Test
    void validToken_parsesSuccessfully_andRejectsExpiredAndWrongClaims() throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();
        RSAPrivateKey priv = (RSAPrivateKey) kp.getPrivate();
        RSAPublicKey pub = (RSAPublicKey) kp.getPublic();

        String issuer = "test-issuer";
        String audience = "test-aud";

        // create a valid token
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject("alice")
                .issuer(issuer)
                .audience(audience)
                .expirationTime(new Date(System.currentTimeMillis() + 60_000))
                .claim("roles", List.of("USER"))
                .build();

        SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.RS256), claims);
        JWSSigner signer = new RSASSASigner(priv);
        jwt.sign(signer);
        String token = jwt.serialize();

        JwtService svc = new JwtService(pemFromPublicKey(pub), issuer, audience, 5);
        var auth = svc.parseToken(token);
        assertThat(auth).isNotNull();
        assertThat(auth.getName()).isEqualTo("alice");

        // expired token
        JWTClaimsSet expiredClaims = new JWTClaimsSet.Builder()
                .subject("bob")
                .issuer(issuer)
                .audience(audience)
                .expirationTime(new Date(System.currentTimeMillis() - 10_000))
                .claim("roles", List.of("USER"))
                .build();
        SignedJWT expired = new SignedJWT(new JWSHeader(JWSAlgorithm.RS256), expiredClaims);
        expired.sign(new RSASSASigner(priv));
        assertThat(svc.parseToken(expired.serialize())).isNull();

        // wrong issuer
        JWTClaimsSet badIss = new JWTClaimsSet.Builder()
                .subject("carol")
                .issuer("bad-issuer")
                .audience(audience)
                .expirationTime(new Date(System.currentTimeMillis() + 60_000))
                .claim("roles", List.of("USER"))
                .build();
        SignedJWT wrongIss = new SignedJWT(new JWSHeader(JWSAlgorithm.RS256), badIss);
        wrongIss.sign(new RSASSASigner(priv));
        assertThat(svc.parseToken(wrongIss.serialize())).isNull();

        // wrong audience
        JWTClaimsSet badAud = new JWTClaimsSet.Builder()
                .subject("dave")
                .issuer(issuer)
                .audience("other-aud")
                .expirationTime(new Date(System.currentTimeMillis() + 60_000))
                .claim("roles", List.of("USER"))
                .build();
        SignedJWT wrongAud = new SignedJWT(new JWSHeader(JWSAlgorithm.RS256), badAud);
        wrongAud.sign(new RSASSASigner(priv));
        assertThat(svc.parseToken(wrongAud.serialize())).isNull();

        // wrong alg (HS256)
        JWTClaimsSet claimsHs = new JWTClaimsSet.Builder()
                .subject("eve")
                .issuer(issuer)
                .audience(audience)
                .expirationTime(new Date(System.currentTimeMillis() + 60_000))
                .claim("roles", List.of("USER"))
                .build();
        SignedJWT hsJwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsHs);
        // sign with MAC
        byte[] secret = new byte[32];
        java.security.SecureRandom.getInstanceStrong().nextBytes(secret);
        hsJwt.sign(new MACSigner(secret));
        assertThat(svc.parseToken(hsJwt.serialize())).isNull();
    }
}
