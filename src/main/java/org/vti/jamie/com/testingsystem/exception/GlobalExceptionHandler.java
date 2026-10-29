package org.vti.jamie.com.testingsystem.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.validation.FieldError;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.vti.jamie.com.testingsystem.dto.responses.ApiResponse;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleNotFound(ResourceNotFoundException ex) {
        return error(HttpStatus.NOT_FOUND, ex.getMessage(), Map.of());
    }

    @ExceptionHandler({DuplicateResourceException.class, ResourceConflictException.class})
    public ResponseEntity<Object> handleConflict(ResourceConflictException ex) {
        return error(HttpStatus.CONFLICT, ex.getMessage(), Map.of());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> handleIntegrity(DataIntegrityViolationException ex) {
        return error(HttpStatus.CONFLICT,
                "Dữ liệu vi phạm ràng buộc của hệ thống", Map.of());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleBadRequest(IllegalArgumentException ex) {
        return error(HttpStatus.BAD_REQUEST, ex.getMessage(), Map.of());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraint(ConstraintViolationException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getConstraintViolations().forEach(v -> errors.putIfAbsent(v.getPropertyPath().toString(), v.getMessage()));
        return error(HttpStatus.BAD_REQUEST, "Dữ liệu đầu vào không hợp lệ", errors);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(e -> errors.putIfAbsent(e.getField(),
                e.getDefaultMessage() == null ? "Giá trị không hợp lệ" : e.getDefaultMessage()));
        return error(HttpStatus.BAD_REQUEST, "Dữ liệu đầu vào không hợp lệ", errors);
    }

    @Override
    protected ResponseEntity<Object> handleHandlerMethodValidationException(HandlerMethodValidationException ex,
                                                                            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        if (ex.isForReturnValue()) {
            log.error("Response validation failed", ex);
            return handleExceptionInternal(ex, null, headers, status, request);
        }
        Map<String, String> errors = new LinkedHashMap<>();
        for (var result : ex.getParameterValidationResults()) {
            String parameter = result.getMethodParameter().getParameterName();
            for (var validationError : result.getResolvableErrors()) {
                String field = validationError instanceof FieldError fieldError ? fieldError.getField()
                        : parameter == null ? "parameter" : parameter;
                errors.putIfAbsent(field, validationError.getDefaultMessage() == null
                        ? "Giá trị không hợp lệ" : validationError.getDefaultMessage());
            }
        }
        return new ResponseEntity<>(ApiResponse.error(400, "Dữ liệu đầu vào không hợp lệ", errors),
                headers, HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body,
                                                             HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        String message = switch (status.value()) {
            case 404 -> "Không tìm thấy endpoint. Vui lòng kiểm tra URL";
            case 405 -> "Phương thức HTTP không được hỗ trợ cho endpoint này";
            case 415 -> "Content-Type không được hỗ trợ";
            case 400 -> "Tham số hoặc nội dung request không hợp lệ";
            default -> status.is5xxServerError() ? "Lỗi hệ thống, vui lòng thử lại sau"
                    : "Không thể xử lý request";
        };

        if (ex instanceof ResponseStatusException responseStatusException
                && status.is4xxClientError()
                && responseStatusException.getReason() != null) {
            message = responseStatusException.getReason();
        }

        return new ResponseEntity<>(ApiResponse.error(status.value(), message, Map.of()),
                headers, status);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleUnexpected(Exception ex) {
        log.error("Unexpected request failure", ex);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi hệ thống, vui lòng thử lại sau", Map.of());
    }

    private ResponseEntity<Object> error(HttpStatus status, String message, Map<String, String> errors) {
        return ResponseEntity.status(status).body(ApiResponse.error(status.value(), message, errors));
    }
}
