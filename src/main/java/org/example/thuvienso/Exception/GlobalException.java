package org.example.thuvienso.Exception;

import jakarta.validation.ConstraintViolation;
import lombok.extern.slf4j.Slf4j;
import org.example.thuvienso.Dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;
import java.util.Objects;

@Slf4j
@ControllerAdvice
public class GlobalException {

    private static final String MIN_ATTRIBUTE = "min";

    // 1. Lỗi nghiệp vụ đã biết trước -> trả đúng status trong ErrorCode
    @ExceptionHandler(AppException.class)
    ResponseEntity<ApiResponse> handleAppException(AppException e) {
        ErrorCode error = e.getErrorCode();
        return buildResponse(error);
    }

    // 2. Lỗi phân quyền (Spring Security ném ra) -> 403
    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ApiResponse> handleAccessDenied(AccessDeniedException e) {
        return buildResponse(ErrorCode.UNAUTHORIZED);
    }

    // 3. Lỗi validate @Valid trên request body -> 400
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse> handleValidationExceptions(MethodArgumentNotValidException e) {
        String enumKey = e.getFieldError().getDefaultMessage();
        ErrorCode error = ErrorCode.INVALID_KEY;
        Map<String, Object> attribute = null;
        try {
            error = ErrorCode.valueOf(enumKey);
            var constraintValidator = e.getBindingResult().getAllErrors()
                    .getFirst().unwrap(ConstraintViolation.class);
            attribute = constraintValidator.getConstraintDescriptor().getAttributes();
        } catch (IllegalArgumentException ignored) {
            // message không map được sang ErrorCode -> giữ INVALID_KEY
        }
        ApiResponse<?> apiResponse = ApiResponse.builder()
                .success(false)
                .code(error.getCode())
                .message(Objects.nonNull(attribute)
                        ? mapAttribute(error.getMessage(), attribute)
                        : error.getMessage())
                .build();
        return ResponseEntity.status(error.getStatus()).body(apiResponse);
    }

    // 4. Mọi lỗi còn lại (KHÔNG xác định) -> 500, và LOG stacktrace để debug
    @ExceptionHandler(RuntimeException.class)
    ResponseEntity<ApiResponse> handleUncategorized(RuntimeException e) {
        log.error("Lỗi không xác định: ", e);
        return buildResponse(ErrorCode.UNCATEGORIZED);
    }

    // 5. Bắt cả các lỗi không phải RuntimeException (IOException của MinIO...)
    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiResponse> handleGeneral(Exception e) {
        log.error("Lỗi hệ thống: ", e);
        return buildResponse(ErrorCode.UNCATEGORIZED);
    }

    private ResponseEntity<ApiResponse> buildResponse(ErrorCode error) {
        ApiResponse<?> apiResponse = ApiResponse.builder()
                .success(false)
                .code(error.getCode())
                .message(error.getMessage())
                .build();
        return ResponseEntity.status(error.getStatus()).body(apiResponse);
    }

    private String mapAttribute(String message, Map<String, Object> attributes) {
        String minValue = String.valueOf(attributes.get(MIN_ATTRIBUTE));
        return message.replace("{" + MIN_ATTRIBUTE + "}", minValue);
    }
}