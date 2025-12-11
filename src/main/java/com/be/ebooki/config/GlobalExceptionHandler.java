package com.be.ebooki.config;

import com.be.ebooki.dto.UserResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    //응답 관련 에러 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationExceptions(MethodArgumentNotValidException ex){
        String msg = ex.getBindingResult().getFieldError().getDefaultMessage();
        return ResponseEntity.badRequest().body(
                UserResponse.UserResponseDTO.<String>builder()
                        .statusCode(400)
                        .message(msg)
                        .data(null)
                        .build()
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgumentExceptions(IllegalArgumentException ex){
        return ResponseEntity.badRequest().body(
                UserResponse.UserResponseDTO.<String>builder()
                        .statusCode(400)
                        .message(ex.getMessage())
                        .data(null)
                        .build()
        );
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<?> handleIllegalStateExceptions(IllegalStateException ex){
        return ResponseEntity.badRequest().body(
                UserResponse.UserResponseDTO.<String>builder()
                        .statusCode(400)
                        .message(ex.getMessage())
                        .data(null)
                        .build()
        );
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleRuntimeExceptions(RuntimeException ex){
        return ResponseEntity.badRequest().body(
                UserResponse.UserResponseDTO.<String>builder()
                        .statusCode(400)
                        .message(ex.getMessage())
                        .data(null)
                        .build()
        );
    }
}
