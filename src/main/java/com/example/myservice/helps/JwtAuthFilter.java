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
        return path.startsWith("/api/v1/auth/login");
    }

    @Override
    public void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            final String authHeader = request.getHeader("Authorization");
            final String jwt;
            final String userID;

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                sendErrorResponse(response,
                        request,
                        HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "Xac thuc khong thanh",
                        "Khong tim thay token"
                );
//                filterChain.doFilter(request, response);
                return;
            }

            jwt = authHeader.substring(7);


            if (!jwtService.isTokenFormatValid(jwt))
            {
                sendErrorResponse(response,
                        request,
                        HttpServletResponse.SC_UNAUTHORIZED,
                        "Xac thuc khong thanh",
                        "Token khong dung dinh dang");
                return;
            }

            if (!jwtService.isIssureToken(jwt))
            {
                sendErrorResponse(response,
                        request,
                        HttpServletResponse.SC_UNAUTHORIZED,
                        "Xac thuc khong thanh",
                        "Nguon goc token khong hop le");
                return;
            }

            if (!jwtService.isSignatureValid(jwt))
            {
                sendErrorResponse(response,
                        request,
                        HttpServletResponse.SC_UNAUTHORIZED,
                        "Xac thuc khong thanh",
                        "Chu ki khong hop le");
                return;
            }

            if (!jwtService.isTokenExpired(jwt))
            {
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

            userID = jwtService.getUserIdFromJwt(jwt);
            if (userID != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = customUserDetailsService.loadUserByUsername(userID);

                final String emailFromToken = jwtService.getEmailFromToken(jwt);
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

        } catch (ServletException | IOException e) {
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
