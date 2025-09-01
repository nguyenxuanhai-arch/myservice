package com.example.myservice.helps;

import com.example.myservice.security.details.CustomUserDetailsService;
import com.example.myservice.resources.ApiResource;
import com.example.myservice.services.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService customUserDetailsService;
    private final ObjectMapper objectMapper;

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthFilter.class);

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/api/v1/auth/login")
                || path.startsWith("/api/v1/auth/refresh")
                || path.startsWith("/api/v1/auth/register");
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        // Không ép 401 khi thiếu token; để Security quyết định (permitAll hoặc authenticated)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);

        try {
            if (!jwtService.isTokenFormatValid(jwt)) {
                throw new BadCredentialsException("Token không đúng định dạng");
            }
            if (!jwtService.isIssureToken(jwt)) { // giữ tên hàm của mày
                throw new BadCredentialsException("Nguồn gốc token không hợp lệ");
            }
            if (!jwtService.isSignatureValid(jwt)) {
                throw new BadCredentialsException("Chữ ký không hợp lệ");
            }
            if (jwtService.isTokenExpired(jwt)) {
                throw new BadCredentialsException("Token đã hết hạn");
            }
            if (jwtService.isBlacklistedToken(jwt)) {
                throw new BadCredentialsException("Token đã bị khóa");
            }

            // === LẤY EMAIL TỪ SUBJECT ===
            final String email = jwtService.getEmailFromToken(jwt);

            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }

            filterChain.doFilter(request, response);

        } catch (ExpiredJwtException e) {
            sendErrorResponse(response, request, HttpStatus.UNAUTHORIZED, "Xác thực thất bại", "Token đã hết hạn");
        } catch (BadCredentialsException e) {
            sendErrorResponse(response, request, HttpStatus.UNAUTHORIZED, "Xác thực thất bại", e.getMessage());
        } catch (RuntimeException e) {
            logger.error("JWT RuntimeException: {}", e.getMessage());
            sendErrorResponse(response, request, HttpStatus.UNAUTHORIZED, "Xác thực thất bại", "Token không hợp lệ");
        }
    }

    private void sendErrorResponse(
            @NotNull HttpServletResponse response,
            @NotNull HttpServletRequest request,
            @NotNull HttpStatus status,
            @NotNull String error,
            @NotNull String message
    ) throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json;charset=UTF-8");

        ApiResource<Object> apiResponse = ApiResource.builder()
                .success(false)
                .status(status)
                .message(error)
                .error(new ApiResource.ErrorResource(
                        String.valueOf(status.value()),
                        error,
                        message + " - Path: " + request.getRequestURI()
                ))
                .build();

        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }
}
