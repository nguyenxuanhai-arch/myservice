package com.example.myservice.services;

import java.security.Key;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.myservice.config.JwtConfig;
import io.jsonwebtoken.security.Keys;
import java.util.function.Function;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import com.example.myservice.modules.users.repositories.BlacklistedTokenRepository;
import com.example.myservice.modules.users.entities.RefreshToken;
import com.example.myservice.modules.users.repositories.RefreshTokenRepository;

@Service
public class JwtService {

    private final JwtConfig jwtConfig;
    private final Key key;
    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);

    @Autowired
    private BlacklistedTokenRepository blacklistedTokenRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    private JwtService(
            JwtConfig jwtConfig
    )
    {
        this.jwtConfig = jwtConfig;
        this.key = Keys.hmacShaKeyFor(Base64.getEncoder().encode(jwtConfig.getSecretKey().getBytes()));
    }

    public String generateToken(Long userId, String email) {
        logger.info("Generating...");
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtConfig.getExpirationTime());

        return Jwts.builder()
                .setSubject(userId.toString())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .setIssuer(jwtConfig.getIssuer())
                .claim("email", email)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    public String generaterefreshToken(Long userId, String email) {
        logger.info("Generating refresh token...");
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtConfig.getRefreshTokenExpirationTime());

        String refreshToken = Jwts.builder()
                .setSubject(userId.toString())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .setIssuer(jwtConfig.getIssuer())
                .claim("email", email)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
        LocalDateTime localExpiryDate = expiryDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

        RefreshToken insertTolen = new RefreshToken();
        insertTolen.setRefreshToken(refreshToken);
        insertTolen.setUserId(userId);
        insertTolen.setExpiryDate(localExpiryDate);
        refreshTokenRepository.save(insertTolen);
        return refreshToken;
    }

    public String getUserIdFromJwt(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
        return claims.getSubject();
    }
    /*
        1. Token dung dinh dang khong
        2. Chu ky token co dung khong
        3. Token het han?
        4. user_id cua token co khop voi userDetails
        5. Kiem tra xem token co trong blacklist ko
        6. Kiem tra quyen
    */

    //1
    public boolean isTokenFormatValid(String token) {
        try {
            String[] tokenParts = token.split("\\.");
            return tokenParts.length == 3;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isSignatureValid(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Key getSigningKey() {
        byte[] keyBytes = jwtConfig.getSecretKey().getBytes();
        return Keys.hmacShaKeyFor(Base64.getEncoder().encode(keyBytes));
    }

    public boolean isTokenExpired(String token) {
        final Date expiryDate = getClaimFromToken(token, Claims::getExpiration);
        return expiryDate.after(new Date());
    }

    public Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public  <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    public boolean isIssureToken(String token) {
        String tokenIssure = getClaimFromToken(token, Claims::getIssuer);
        return jwtConfig.getIssuer().equals(tokenIssure);
    }

    public String getEmailFromToken(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
        return claims.get("email", String.class);
    }
    public boolean isBlacklistedToken(String token) {
        return blacklistedTokenRepository.existsByToken(token);
    }

    public boolean isRefreshTokenValid(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
            RefreshToken refreshToken = refreshTokenRepository.findByRefreshToken(token).orElseThrow(() -> new
                    RuntimeException("Refresh khong ton tai"));
            final Date expiryDate = getClaimFromToken(token, Claims::getExpiration);

            return expiryDate.after(new Date());
        } catch (Exception e) {
            return false;
        }
    }
}