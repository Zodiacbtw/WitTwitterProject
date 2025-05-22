package com.twitter.api.security.jwt;

import io.jsonwebtoken.*; // JwtException, ExpiredJwtException vb. için
import io.jsonwebtoken.security.Keys;
// io.jsonwebtoken.security.SignatureException ve io.jsonwebtoken.security.SecurityException için import GEREKMİYOR,
// çünkü bunları genel JwtException ile yakalayacağız veya spesifik olanı zaten JwtException'dan türer.
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration}")
    private int jwtExpirationMs;

    @PostConstruct
    public void logSecretForDebug() {
        if (jwtSecret == null || jwtSecret.isEmpty()) {
            logger.error("CRITICAL: JwtUtils initialized - jwtSecret IS NULL OR EMPTY! JWT generation/validation will fail.");
        } else {
            logger.info("JwtUtils initialized with jwtSecret (first 5 chars for verification, DO NOT LOG FULL SECRET IN PRODUCTION): '{}...'", jwtSecret.substring(0, Math.min(jwtSecret.length(), 5)));
            if (jwtSecret.getBytes(StandardCharsets.UTF_8).length * 8 < 256 && SignatureAlgorithm.HS256.isHmac()) {
                 logger.warn("WARNING: JWT Secret may be too short for HS256. Recommended length is at least 32 bytes (256 bits). Current length: {} bytes", jwtSecret.getBytes(StandardCharsets.UTF_8).length);
            }
        }
        logger.info("JwtUtils initialized with jwtExpirationMs: {}", jwtExpirationMs);
    }

    public String generateJwtToken(Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        logger.info("JwtUtils: generateJwtToken CALLED for user: {}", userPrincipal.getUsername());

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        logger.debug("JwtUtils: Generating token for subject: {}, issuedAt: {}, expiresAt: {}", userPrincipal.getUsername(), now, expiryDate);

        String token = Jwts.builder()
                .setSubject(userPrincipal.getUsername())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
        logger.info("JwtUtils: JWT generated successfully for user: {}. Token (first 10 chars): {}...", userPrincipal.getUsername(), (token.length() > 10 ? token.substring(0, 10) : token));
        return token;
    }

    private Key key() {
        if (jwtSecret == null || jwtSecret.isEmpty()) {
            logger.error("JwtUtils (key method): jwtSecret is NULL or EMPTY when trying to create key. This will cause an error.");
            throw new IllegalStateException("JWT Secret is not configured properly.");
        }
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String getUserNameFromJwtToken(String token) {
        logger.debug("JwtUtils: getUserNameFromJwtToken CALLED. Attempting to extract username.");
        try {
            String username = Jwts.parserBuilder().setSigningKey(key()).build()
                    .parseClaimsJws(token).getBody().getSubject();
            logger.info("JwtUtils: Successfully extracted username: {} from token.", username);
            return username;
        } catch (Exception e) {
            logger.error("JwtUtils: Failed to extract username from token: {}. Token (first 10 chars): {}...", e.getMessage(), (token != null && token.length() > 10 ? token.substring(0, 10) : token), e);
            throw e;
        }
    }

    public boolean validateJwtToken(String authToken) {
        logger.info("JwtUtils: validateJwtToken CALLED. Token (first 10 chars if present): {}", (authToken != null && authToken.length() > 10 ? authToken.substring(0, 10) + "..." : authToken));
        try {
            Jwts.parserBuilder().setSigningKey(key()).build().parseClaimsJws(authToken);
            logger.info("JwtUtils: Token validation successful.");
            return true;
        } catch (ExpiredJwtException e) { // ExpiredJwtException, JwtException'dan türediği için önce onu yakalamak iyi bir pratik.
            logger.error("JwtUtils: JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) { // UnsupportedJwtException da JwtException'dan türer.
            logger.error("JwtUtils: JWT token is unsupported: {}", e.getMessage());
        } catch (MalformedJwtException e) { // MalformedJwtException da JwtException'dan türer.
            logger.error("JwtUtils: Invalid JWT token (malformed): {}", e.getMessage());
        } catch (io.jsonwebtoken.security.SignatureException e) { // Bu, JJWT'nin imza hataları için kullandığı spesifik exception'dır.
            logger.error("JwtUtils: Invalid JWT signature: {}", e.getMessage());
        } catch (IllegalArgumentException e) { // Bu genellikle token string'inin null/boş olmasıyla veya geçersiz argümanlarla ilgili.
            logger.error("JwtUtils: JWT claims string is empty or token string is invalid/null: {}", e.getMessage());
        }
        // Genel JwtException'ı en sona alabiliriz, yukarıdakiler daha spesifik olduğu için.
        // Veya yukarıdaki spesifik JwtException'lar (Expired, Unsupported, Malformed, Signature) zaten JwtException'dan türediği için,
        // sadece bir tane genel JwtException catch bloğu da yeterli olabilir.
        // Ancak, farklı tür hatalar için farklı log mesajları vermek istiyorsak spesifik olanları tutmak daha iyi.
        // Şu an için `io.jsonwebtoken.security.SecurityException`'ı çıkardım çünkü `SignatureException` daha spesifik.
        // Eğer hala derleme hatası verirse, o zaman sadece `catch (JwtException e)` kullanabiliriz.
        catch (JwtException e) { // Diğer tüm JJWT'ye özgü runtime hatalarını kapsar.
            logger.error("JwtUtils: General JWT validation error: {}. Exception Type: {}", e.getMessage(), e.getClass().getSimpleName());
        } catch (Exception e) { // Diğer beklenmedik (JJWT dışı) runtime hataları için genel bir catch
            logger.error("JwtUtils: Unexpected error during JWT validation: {}", e.getMessage(), e);
        }
        logger.warn("JwtUtils: Token validation FAILED for token (first 10 chars): {}...", (authToken != null && authToken.length() > 10 ? authToken.substring(0, 10) : authToken));
        return false;
    }
}