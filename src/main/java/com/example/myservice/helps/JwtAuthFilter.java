package com.example.myservice.helps;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.MediaType;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import com.example.myservice.services.JwtService;
import org.springframework.lang.NonNull;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.UserDetails;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import io.jsonwebtoken.ExpiredJwtException; // Import ExpiredJwtException

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.myservice.modules.users.services.impl.UserService;
import com.example.myservice.modules.users.services.impl.CustomUserDetailsService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

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
        return path.startsWith("/api/v1/auth/login") || path.startsWith("/api/v1/auth/refresh");
    }

    @Override
    public void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userID;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendErrorResponse(response,
                    request,
                    HttpServletResponse.SC_UNAUTHORIZED, // Changed to 401 as it's authentication error
                    "Xac thuc khong thanh",
                    "Khong tim thay token"
            );
            return;
        }

        jwt = authHeader.substring(7);

        try {
            if (!jwtService.isTokenFormatValid(jwt)) {
                sendErrorResponse(response, request, HttpServletResponse.SC_UNAUTHORIZED, "Xac thuc khong thanh", "Token khong dung dinh dang");
                return;
            }

            // Lưu ý: isIssureToken, isSignatureValid và isTokenExpired
            // đều gọi đến getAllClaimsFromToken, có thể ném ExpiredJwtException.
            // Do đó, việc đặt chúng trong khối try-catch lớn này là cần thiết.
            if (!jwtService.isIssureToken(jwt)) {
                sendErrorResponse(response,
                        request,
                        HttpServletResponse.SC_UNAUTHORIZED,
                        "Xac thuc khong thanh",
                        "Nguon goc token khong hop le");
                return;
            }

            if (!jwtService.isSignatureValid(jwt)) {
                sendErrorResponse(response,
                        request,
                        HttpServletResponse.SC_UNAUTHORIZED,
                        "Xac thuc khong thanh",
                        "Chu ki khong hop le");
                return;
            }

            // `isTokenExpired` có khối catch riêng để xử lý ExpiredJwtException
            // nhưng việc ném lại từ `getAllClaimsFromToken` sẽ bắt được ở đây.
            if (jwtService.isTokenExpired(jwt)) {
                sendErrorResponse(response,
                        request,
                        HttpServletResponse.SC_UNAUTHORIZED,
                        "Xac thuc khong thanh",
                        "Token da het han");
                return;
            }

            if (jwtService.isBlacklistedToken(jwt)) {
                sendErrorResponse(response,
                        request,
                        HttpServletResponse.SC_UNAUTHORIZED,
                        "Xac thuc khong thanh",
                        "Token bi khoa");
                return;
            }

            userID = jwtService.getUserIdFromJwt(jwt); // Cũng có thể ném ExpiredJwtException
            if (userID != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = customUserDetailsService.loadUserByUsername(userID);

                final String emailFromToken = jwtService.getEmailFromToken(jwt); // Cũng có thể ném ExpiredJwtException
                if (!emailFromToken.equals(userDetails.getUsername())) {
                    sendErrorResponse(response,
                            request,
                            HttpServletResponse.SC_UNAUTHORIZED,
                            "Xac thuc khong thanh",
                            "User Token khong chinh xac");
                    return;
                }

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                SecurityContextHolder.getContext().setAuthentication(authToken);
                logger.info("Xac thuc tai khoan thanh cong!" + userDetails.getUsername());
            }

            filterChain.doFilter(request, response);

        } catch (ExpiredJwtException e) {
            // Bắt ExpiredJwtException cụ thể ở đây
            logger.warn("Token đã hết hạn: {}", e.getMessage());
            sendErrorResponse(response,
                    request,
                    HttpServletResponse.SC_UNAUTHORIZED, // 401 Unauthorized
                    "Xac thuc khong thanh",
                    "Token da het han");
            return;
        } catch (RuntimeException e) { // Bắt các RuntimeException khác từ JwtService
            // Phân loại các loại lỗi RuntimeException cụ thể hơn nếu cần
            if (e.getMessage() != null && e.getMessage().contains("Chữ ký token không hợp lệ")) {
                sendErrorResponse(response, request, HttpServletResponse.SC_UNAUTHORIZED, "Xac thuc khong thanh", "Chu ki khong hop le");
            } else if (e.getMessage() != null && e.getMessage().contains("Token không đúng định dạng")) {
                sendErrorResponse(response, request, HttpServletResponse.SC_UNAUTHORIZED, "Xac thuc khong thanh", "Token khong dung dinh dang");
            }
            // Có thể thêm các điều kiện else if cho các loại RuntimeException khác mà bạn đã ném
            else {
                logger.error("Lỗi xử lý token không xác định: {}", e.getMessage(), e);
                sendErrorResponse(response, request, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi xử lý token", "Đã xảy ra lỗi không mong muốn khi xử lý token.");
            }
            return;
        } catch (ServletException | IOException e) {
            // Khối catch hiện có cho ServletException/IOException
            logger.error("Lỗi mạng hoặc Servlet trong filter: {}", e.getMessage(), e);
            sendErrorResponse(response,
                    request,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Network Error!",
                    e.getMessage()
            );
            return;
        }
    }

    private void sendErrorResponse(
            @NotNull HttpServletResponse response,
            @NotNull HttpServletRequest request,
            int statusCode,
            String error,
            String message
    ) throws IOException {
        response.setStatus(statusCode);
        response.setContentType("application/json;charset=UTF-8");

        Map<String, Object> errorRespone = new HashMap<>();

        errorRespone.put("timestamp", System.currentTimeMillis());
        errorRespone.put("status", statusCode);
        errorRespone.put("error", error);
        errorRespone.put("message", message);
        errorRespone.put("path", request.getRequestURI());

        String jsonRespone = objectMapper.writeValueAsString(errorRespone);

        response.getWriter().write(jsonRespone);
    }
}