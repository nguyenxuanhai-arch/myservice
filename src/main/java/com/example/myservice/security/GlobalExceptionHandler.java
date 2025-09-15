package com.example.myservice.security;

import com.example.myservice.resources.ApiResource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    // ==== HTTP layer common ====

    // 405 METHOD NOT ALLOWED
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResource<Object>> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        Set<String> supported = ex.getSupportedHttpMethods() == null
                ? Set.of()
                : ex.getSupportedHttpMethods().stream().map(HttpMethod::name).collect(Collectors.toSet());

        String detail = supported.isEmpty()
                ? "Phương thức không được hỗ trợ cho endpoint này."
                : "Hỗ trợ: " + String.join(", ", supported);

        ApiResource<Object> body = ApiResource.builder()
                .success(false)
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .error(new ApiResource.ErrorResource("METHOD_NOT_ALLOWED", "Request method không được hỗ trợ", detail))
                .build();

        HttpHeaders headers = new HttpHeaders();
        if (!supported.isEmpty()) headers.setAllow(ex.getSupportedHttpMethods());
        return new ResponseEntity<>(body, headers, HttpStatus.METHOD_NOT_ALLOWED);
    }

    // 415 UNSUPPORTED MEDIA TYPE
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiResource<Object>> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex) {
        String supported = ex.getSupportedMediaTypes().stream().map(MediaType::toString).collect(Collectors.joining(", "));
        String detail = supported.isBlank() ? "Content-Type không được hỗ trợ." : "Hỗ trợ: " + supported;

        return buildApiError(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                "UNSUPPORTED_MEDIA_TYPE",
                "Content-Type không được hỗ trợ",
                detail);
    }

    // 406 NOT ACCEPTABLE
    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<ApiResource<Object>> handleNotAcceptable(HttpMediaTypeNotAcceptableException ex) {
        String supported = ex.getSupportedMediaTypes().stream().map(MediaType::toString).collect(Collectors.joining(", "));
        String detail = supported.isBlank() ? "Accept không phù hợp." : "Có thể trả về: " + supported;

        return buildApiError(HttpStatus.NOT_ACCEPTABLE,
                "NOT_ACCEPTABLE",
                "Không hỗ trợ định dạng phản hồi theo Accept header",
                detail);
    }

    // 400 MISSING REQUEST PARAM
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResource<Object>> handleMissingParam(MissingServletRequestParameterException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        errors.put(ex.getParameterName(), "Thiếu tham số");
        return buildError("Thiếu tham số yêu cầu", errors);
    }

    // 400 TYPE MISMATCH (e.g. id = abc thay vì số)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResource<Object>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String name = ex.getName();
        String required = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown";
        Map<String, String> errors = new LinkedHashMap<>();
        errors.put(name, "Sai kiểu dữ liệu ( cần " + required + " )");
        return buildError("Kiểu tham số không hợp lệ", errors);
    }

    // 404 NO HANDLER (bật cấu hình bên dưới để Spring ném exception này)
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResource<Object>> handleNoHandler(HttpServletRequest req) {
        String detail = "Không tìm thấy endpoint: " + req.getRequestURI();
        return buildApiError(HttpStatus.NOT_FOUND, "NOT_FOUND", "Endpoint không tồn tại", detail);
    }

    // ==== Validation/binding ====

    // Body validation (@Valid @RequestBody ...)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResource<Object>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        return buildError("Dữ liệu body không hợp lệ", errors);
    }

    // Binding error (thường khi convert kiểu fail)
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResource<Object>> handleBindException(BindException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            errors.put(fe.getField(), fe.getDefaultMessage());
        }
        return buildError("Dữ liệu không hợp lệ", errors);
    }

    // ==== Catch-all fallback ====
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResource<Object>> handleOther() {
        return buildApiError(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR",
                "Có lỗi xảy ra", "Tham số không hỗ trợ hoặc lỗi server.");
    }

    // ==== Helpers ====

    // ApiResource với message + map errors (dùng cho 400 validation)
    private ResponseEntity<ApiResource<Object>> buildError(String message, Map<String, String> errors) {
        var body = ApiResource.errors(errors, message, HttpStatus.BAD_REQUEST);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // ApiResource với ErrorResource(code, message, detail)
    private ResponseEntity<ApiResource<Object>> buildApiError(HttpStatus status, String code, String message, String detail) {
        var body = ApiResource.errorDetail(code, message, detail, status);
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(InvalidDataAccessApiUsageException.class)
    public ResponseEntity<ApiResource<Object>> handleInvalidDataAccess(
            InvalidDataAccessApiUsageException ex) {

        String rootMsg = Optional.ofNullable(NestedExceptionUtils.getMostSpecificCause(ex).getMessage())
                .orElse(ex.getMessage());

        String badAttr = extractBadAttribute(rootMsg);

        Map<String, String> errors = new LinkedHashMap<>();
        if (badAttr != null) errors.put(badAttr, "Không được hỗ trợ");

        var body = ApiResource.errors(errors, "Tham số không hợp lệ", HttpStatus.BAD_REQUEST);

        return ResponseEntity.badRequest().body(body);
    }

    private String extractBadAttribute(String msg) {
        if (msg == null) return null;
        Matcher m = Pattern.compile("attribute '([^']+)'").matcher(msg);
        return m.find() ? m.group(1) : null;
    }

}
