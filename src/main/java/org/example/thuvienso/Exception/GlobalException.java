package org.example.thuvienso.Exception;


import jakarta.validation.ConstraintViolation;
import lombok.extern.slf4j.Slf4j;
import org.example.thuvienso.Dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;
import java.util.Objects;

@Slf4j
@ControllerAdvice
public class GlobalException {
    public GlobalException() {
    }
    private static final String MIN_ATTRIBUTE="min";
    //này để bắt các lỗi nằm ngoài
    @ExceptionHandler(value=RuntimeException.class)
    ResponseEntity<ApiResponse> handleException(RuntimeException e){
        ApiResponse apiResponse= ApiResponse.builder()
                .success(false)
                .code(ErrorCode.UNCATEGORIZED.getCode())
                .message(ErrorCode.UNCATEGORIZED.getMessage())
                .build();
        return ResponseEntity.badRequest().body(apiResponse);
    }
    //này để bắt các lỗi da biết trước
    @ExceptionHandler(value= AppException.class)
    ResponseEntity<ApiResponse> handleAppException(AppException e){
        ErrorCode error=e.getErrorCode();
        ApiResponse apiResponse=ApiResponse.builder()
                .success(false)
                .code(error.getCode())
                .message(error.getMessage())
                .build();
        return ResponseEntity.status(error.getStatus()).body(apiResponse);
    }
    /*@ExceptionHandler(value= AccessDeniedException.class)
    ResponseEntity<ApiResponse> handleAccessDenied(AccessDeniedException e, Throwable t){
        ErrorCode error=ErrorCode.UNAUTHORIZED;
        return ResponseEntity.status(error.getStatus()).body(
                ApiResponse.builder()
                        .success(false)
                        .code(error.getCode())
                        .message(error.getMessage())
                        .build()
        );
    }*/
    //này đe bat cac lỗi có trong Entity
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse> handleValidationExceptions(MethodArgumentNotValidException e){
        String code=e.getFieldError().getDefaultMessage();
        ErrorCode error=ErrorCode.INVALID_KEY;
        Map<String,Object> attribute=null;
        try{
            error=ErrorCode.valueOf(code);
            var constraintValidator=e.getBindingResult().getAllErrors()
                    .getFirst().unwrap(ConstraintViolation.class);
            attribute=constraintValidator.getConstraintDescriptor().getAttributes();
            log.info(attribute.toString());
        } catch (IllegalArgumentException e1){

        }
        ApiResponse apiResponse= new ApiResponse();
        apiResponse.setSuccess(false);
        apiResponse.setCode(error.getCode());
        apiResponse.setMessage(Objects.nonNull(attribute) ?
                mapAttribute(error.getMessage(),attribute)
                : error.getMessage());
        return ResponseEntity.badRequest().body(apiResponse);
    }
    private String mapAttribute(String message, Map<String,Object> attributes){
        String minValue=String.valueOf(attributes.get(MIN_ATTRIBUTE));
        return  message.replace("{"+MIN_ATTRIBUTE+"}",minValue);
    }
}
