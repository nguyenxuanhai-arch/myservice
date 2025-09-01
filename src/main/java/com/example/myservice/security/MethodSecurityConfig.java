package com.example.myservice.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;

@Configuration
public class MethodSecurityConfig {
    @Bean
    public MethodSecurityExpressionHandler methodSecurityExpressionHandler(PermissionEvaluator pe) {
        var h = new DefaultMethodSecurityExpressionHandler();
        h.setPermissionEvaluator(pe);
        return h;
    }
}


