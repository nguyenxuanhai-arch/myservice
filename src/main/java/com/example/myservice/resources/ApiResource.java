package com.example.myservice.resources;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.springframework.http.HttpStatus;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResource<T> {
    private boolean success;
    private String message;
    private T data;
    private HttpStatus status;
    private LocalDateTime timestamp;
    private ErrorResource error;
    private Map<String, String> errors;

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ErrorResource {
        private String code;
        private String message;
        private String detail;

        public ErrorResource(String message) {
            this.message = message;
        }

        public ErrorResource(String code, String message) {
            this.code = code;
            this.message = message;
        }

        public ErrorResource(String code, String message, String detail) {
            this.code = code;
            this.message = message;
            this.detail = detail;
        }
    }

    private ApiResource() {
        this.timestamp = LocalDateTime.now();
    }

    public static <T> Builder <T> builder() {
        return new Builder<>();
    }
    public static class Builder<T> {
        private final ApiResource<T> resource;

        public Builder() {
            resource = new ApiResource<>();
        }

        public Builder<T> success(boolean success) {
            resource.success = success;
            return this;
        }

        public Builder<T> message(String message) {
            resource.message = message;
            return this;
        }

        public Builder<T> data(T data) {
            resource.data =data;
            return this;
        }

        public Builder<T> status(HttpStatus status) {
            resource.status = status;
            return this;
        }

        public Builder<T> error(ErrorResource error) {
            resource.error = error;
            return this;
        }

        public ApiResource<T> build() {
            return resource;
        }

        public Builder<T> errors(Map<String, String> errors) {
            resource.errors = errors;
            return this;
        }
    }

    public static <T> ApiResource<T> ok(T data, String message) {
        return ApiResource.<T>builder()
                .success(true)
                .data(data)
                .message(message)
                .status(HttpStatus.OK)
                .build();
    }

    public static <T> ApiResource<T> message(String message, HttpStatus status) {
        return ApiResource.<T>builder()
                .success(true)
                .message(message)
                .status(status)
                .build();
    }

    public static <T> ApiResource<T> error(String code, String message, HttpStatus status) {
        return ApiResource.<T>builder()
                .success(false)
                .error(new ErrorResource(code, message))
                .status(status)
                .build();
    }
}
