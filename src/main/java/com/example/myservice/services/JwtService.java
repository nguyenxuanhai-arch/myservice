package com.example.myservice.services;

import java.security.Key;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Base64;
import io.jsonwebtoken.ExpiredJwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.myservice.config.JwtConfig;
import io.jsonwebtoken.security.Keys;
import java.util.Optional;
import java.util.UUID;
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

    public JwtService(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
        this.key = Keys.hmacShaKeyFor(Base64.getEncoder().encode(jwtConfig.getSecretKey().getBytes()));
    }

    public String generateToken(Long userId, String email) {
        logger.info("Generating...");
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtConfig.getExpirationTime());

        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("email", email)
                .setIssuer(jwtConfig.getIssuer())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    public String generateRefreshToken(Long userId, String email) {
        logger.info("Generating refresh token...");
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtConfig.getRefreshTokenExpirationTime());

        String refreshToken = UUID.randomUUID().toString();

        LocalDateTime localExpiryDate = expiryDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

        Optional<RefreshToken> optionalRefreshToken = refreshTokenRepository.findByUserId(userId);
        if (optionalRefreshToken.isPresent()) {
            RefreshToken dBRefreshToken = optionalRefreshToken.get();
            dBRefreshToken.setRefreshToken(refreshToken);
            dBRefreshToken.setExpiryDate(localExpiryDate);
            refreshTokenRepository.save(dBRefreshToken);
        } else {
            RefreshToken insertToken = new RefreshToken();
            insertToken.setRefreshToken(refreshToken);
            insertToken.setUserId(userId);
            insertToken.setExpiryDate(localExpiryDate);
            refreshTokenRepository.save(insertToken);
        }
        return refreshToken;
    }

    public String getUserIdFromJwt(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
        return claims.getSubject();
    }

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
        try {
            Date expiration = getClaimFromToken(token, Claims::getExpiration);
            return expiration.before(new Date());
        } catch (ExpiredJwtException e) {
            // Nếu ExpiredJwtException được ném lên từ getAllClaimsFromToken/getClaimFromToken
            // thì chắc chắn là token đã hết hạn.
            return true;
        } catch (RuntimeException e) {
            // Bắt các RuntimeException khác (như "Chữ ký token không hợp lệ", "Token không đúng định dạng")
            // từ getAllClaimsFromToken và coi chúng là token không hợp lệ.
            logger.warn("Token không hợp lệ (không phải do hết hạn): {}", e.getMessage());
            return true; // Trả về true để coi token không hợp lệ
        }
    }

    public Claims getAllClaimsFromToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            // Ném lại chính ExpiredJwtException
            throw e;
        } catch (io.jsonwebtoken.security.SignatureException e) {
            // Bắt lỗi chữ ký không hợp lệ
            logger.error("Lỗi chữ ký token: {}", e.getMessage());
            throw new RuntimeException("Chữ ký token không hợp lệ", e);
        } catch (io.jsonwebtoken.MalformedJwtException e) {
            // Bắt lỗi định dạng token không đúng
            logger.error("Lỗi định dạng token: {}", e.getMessage());
            throw new RuntimeException("Token không đúng định dạng", e);
        } catch (Exception e) {
            // Bắt các lỗi chung khác
            logger.error("Lỗi khi phân tích token: {}", e.getMessage());
            throw new RuntimeException("Lỗi không xác định khi phân tích token", e);
        }
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    public boolean isIssureToken(String token) {
        String tokenIssuer = getClaimFromToken(token, Claims::getIssuer);
        return jwtConfig.getIssuer().equals(tokenIssuer);
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
            RefreshToken refreshToken = refreshTokenRepository.findByRefreshToken(token).orElseThrow(() ->
                    new RuntimeException("Refresh token không tồn tại"));

            LocalDateTime expirytionLocalDateTime = refreshToken.getExpiryDate();
            Date expirationDate = Date.from(expirytionLocalDateTime.atZone(ZoneId.systemDefault()).toInstant());
            return expirationDate.after(new Date());
        } catch (Exception e) {
            return false;
        }
    }
}
