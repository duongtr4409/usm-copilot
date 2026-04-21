package com.usm.ams.security;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class JwtService {

    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);

    private final RSAPublicKey publicKey;
    private final String issuer;
    private final String audience;
    private final long leewaySeconds;

    public JwtService(@Value("${JWT_PUBLIC_KEY:}") String publicKeyPem,
                      @Value("${security.jwt.issuer:}") String issuer,
                      @Value("${security.jwt.audience:}") String audience,
                      @Value("${security.jwt.leeway_seconds:60}") long leewaySeconds) throws Exception {
        this.issuer = (issuer == null) ? "" : issuer;
        this.audience = (audience == null) ? "" : audience;
        this.leewaySeconds = leewaySeconds;

        if (publicKeyPem == null || publicKeyPem.isBlank()) {
            if (!this.issuer.isBlank() || !this.audience.isBlank()) {
                throw new IllegalStateException("JWT is configured (issuer/audience set) but public key is missing");
            }
            this.publicKey = null;
            logger.warn("No JWT public key configured; JwtService will not validate tokens.");
        } else {
            this.publicKey = (RSAPublicKey) readPublicKey(publicKeyPem);
        }
    }

    private PublicKey readPublicKey(String pem) throws Exception {
        String normalized = pem.replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s+", "");
        byte[] decoded = Base64.getDecoder().decode(normalized);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(spec);
    }

    /**
     * Validate and convert a JWT token to an Authentication. Returns null if invalid.
     */
    public Authentication parseToken(String token) {
        try {
            if (publicKey == null) return null;
            SignedJWT jwt = SignedJWT.parse(token);

            // enforce algorithm
            JWSAlgorithm alg = jwt.getHeader().getAlgorithm();
            if (!JWSAlgorithm.RS256.equals(alg)) {
                logger.warn("Rejecting JWT with unexpected alg={}", alg);
                return null;
            }

            RSASSAVerifier verifier = new RSASSAVerifier(publicKey);
            if (!jwt.verify(verifier)) {
                logger.debug("JWT signature verification failed");
                return null;
            }

            JWTClaimsSet claims = jwt.getJWTClaimsSet();
            Date now = new Date();

            Date exp = claims.getExpirationTime();
            if (exp == null) {
                logger.debug("JWT missing exp claim");
                return null;
            }
            long leewayMs = leewaySeconds * 1000L;
            if (exp.getTime() + leewayMs < now.getTime()) {
                logger.debug("JWT expired at {} (now={})", exp, now);
                return null;
            }

            Date nbf = claims.getNotBeforeTime();
            if (nbf != null && nbf.getTime() - leewayMs > now.getTime()) {
                logger.debug("JWT not valid yet nbf={} (now={})", nbf, now);
                return null;
            }

            if (!this.issuer.isBlank()) {
                String tokenIss = claims.getIssuer();
                if (tokenIss == null || !tokenIss.equals(this.issuer)) {
                    logger.debug("JWT issuer mismatch expected='{}' token='{}'", this.issuer, tokenIss);
                    return null;
                }
            }

            if (!this.audience.isBlank()) {
                List<String> aud = claims.getAudience();
                if (aud == null || !aud.contains(this.audience)) {
                    logger.debug("JWT audience mismatch expected='{}' token='{}'", this.audience, aud);
                    return null;
                }
            }

            String username = claims.getSubject();
            if (username == null) {
                logger.debug("JWT missing subject");
                return null;
            }

            List<String> roles = claims.getStringListClaim("roles");
            List<GrantedAuthority> authorities = (roles == null) ? List.of() : roles.stream()
                    .map(this::normalizeRole)
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());

            return new UsernamePasswordAuthenticationToken(username, null, authorities);
        } catch (Exception e) {
            logger.debug("Failed to parse/validate JWT: {}", e.getMessage());
            return null;
        }
    }

    private String normalizeRole(String r) {
        if (r == null) return null;
        if (r.startsWith("ROLE_")) return r;
        return "ROLE_" + r;
    }
}
