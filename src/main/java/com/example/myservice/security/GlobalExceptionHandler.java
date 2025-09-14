package com.example.myservice.security;

import com.example.myservice.resources.ApiResource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Body validation (@Valid @RequestBody ...)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResource<Object>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        return buildError(HttpStatus.BAD_REQUEST, "Dữ liệu body không hợp lệ", errors);
    }

    // Param/path validation (@PathVariable, @RequestParam, ...), Spring 6+
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiResource<Object>> handleHandlerMethodValidation(HandlerMethodValidationException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getAllValidationResults().forEach(r -> {
            String param = r.getMethodParameter().getParameterName();
            r.getResolvableErrors().forEach(err -> {
                errors.merge(param, err.getDefaultMessage(), (a, b) -> a + "; " + b);
            });
        });
        return buildError(HttpStatus.BAD_REQUEST, "Tham số không hợp lệ", errors);
    }

    // Binding error (thường khi convert kiểu fail)
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResource<Object>> handleBindException(BindException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            errors.put(fe.getField(), fe.getDefaultMessage());
        }
        return buildError(HttpStatus.BAD_REQUEST, "Dữ liệu không hợp lệ", errors);
    }

    // Request body trống, JSON lỗi
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResource<Object>> handleNotReadable(HttpMessageNotReadableException ex) {
        return buildError(HttpStatus.BAD_REQUEST, "Payload không hợp lệ hoặc thiếu body", null);
    }

    // Catch-all fallback
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResource<Object>> handleOther(Exception ex, HttpServletRequest req) {
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), null);
    }

    // Helper để build ApiResource error
    private ResponseEntity<ApiResource<Object>> buildError(HttpStatus status, String message, Map<String, String> errors) {
        var body = ApiResource.<Object>builder()
                .success(false)
                .message(message)
                .status(status)
                .errors(errors)
                .build();
        return ResponseEntity.status(status).body(body);
    }
}
