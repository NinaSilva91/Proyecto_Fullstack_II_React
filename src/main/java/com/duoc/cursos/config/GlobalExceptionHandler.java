package com.duoc.cursos.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @Data
    public static class ErrorResponse {
        private final String error;
        private final String mensaje;
        private final int status;
        private final Map<String, String> detalles;

        public ErrorResponse(String error, String mensaje, int status) {
            this.error = error;
            this.mensaje = mensaje;
            this.status = status;
            this.detalles = new HashMap<>();
        }
    }

    // --- Excepciones específicas de Cognito ---

    @ExceptionHandler(UsernameExistsException.class)
    public ResponseEntity<ErrorResponse> handleUsernameExists(UsernameExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("CONFLICT", "El email ya está registrado", HttpStatus.CONFLICT.value()));
    }

    @ExceptionHandler(NotAuthorizedException.class)
    public ResponseEntity<ErrorResponse> handleNotAuthorized(NotAuthorizedException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse("UNAUTHORIZED", "Credenciales inválidas", HttpStatus.UNAUTHORIZED.value()));
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPassword(InvalidPasswordException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("BAD_REQUEST", "La contraseña no cumple los requisitos: " + ex.getMessage(), HttpStatus.BAD_REQUEST.value()));
    }

    @ExceptionHandler(CodeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleCodeMismatch(CodeMismatchException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("BAD_REQUEST", "Código de verificación inválido o expirado", HttpStatus.BAD_REQUEST.value()));
    }

    @ExceptionHandler(ExpiredCodeException.class)
    public ResponseEntity<ErrorResponse> handleExpiredCode(ExpiredCodeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("BAD_REQUEST", "El código de verificación ha expirado", HttpStatus.BAD_REQUEST.value()));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("NOT_FOUND", "Usuario no encontrado", HttpStatus.NOT_FOUND.value()));
    }

    // La cuenta existe pero no se confirmó todavía (código de verificación del email)
    @ExceptionHandler(UserNotConfirmedException.class)
    public ResponseEntity<ErrorResponse> handleUserNotConfirmed(UserNotConfirmedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse("USER_NOT_CONFIRMED",
                        "Tu cuenta está sin verificar: revisa el código que te llegó al correo e ingrésalo para activarla",
                        HttpStatus.FORBIDDEN.value()));
    }

    @ExceptionHandler(TooManyRequestsException.class)
    public ResponseEntity<ErrorResponse> handleTooManyRequests(TooManyRequestsException ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(new ErrorResponse("TOO_MANY_REQUESTS", "Demasiadas solicitudes. Intente más tarde", HttpStatus.TOO_MANY_REQUESTS.value()));
    }

    @ExceptionHandler(LimitExceededException.class)
    public ResponseEntity<ErrorResponse> handleLimitExceeded(LimitExceededException ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(new ErrorResponse("TOO_MANY_REQUESTS", "Límite excedido: " + ex.getMessage(), HttpStatus.TOO_MANY_REQUESTS.value()));
    }

    @ExceptionHandler(InvalidParameterException.class)
    public ResponseEntity<ErrorResponse> handleInvalidParameter(InvalidParameterException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("BAD_REQUEST", "Parámetro inválido: " + ex.getMessage(), HttpStatus.BAD_REQUEST.value()));
    }

    // --- Validación de DTOs ---
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        ErrorResponse error = new ErrorResponse("VALIDATION_ERROR", "Datos de entrada inválidos", HttpStatus.BAD_REQUEST.value());
        ex.getBindingResult().getFieldErrors().forEach(err ->
                error.getDetalles().put(err.getField(), err.getDefaultMessage())
        );
        return ResponseEntity.badRequest().body(error);
    }

    // --- Excepciones de negocio propias ---
    @ExceptionHandler(com.duoc.cursos.common.exception.BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(com.duoc.cursos.common.exception.BusinessException ex) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        if ("RESOURCE_NOT_FOUND".equals(ex.getCode())) {
            status = HttpStatus.NOT_FOUND;
        }
        return ResponseEntity.status(status)
                .body(new ErrorResponse(ex.getCode(), ex.getMessage(), status.value()));
    }

    // --- Ruta no encontrada ---
    // Spring Boot 3.2+: una petición a una URL sin controlador lanza NoResourceFoundException.
    // Sin este handler, el generic(Exception) la devolvía como 500 en lugar de 404,
    // y se perdía la info de qué ruta falló (clave para depurar el frontend)
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFound(NoResourceFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("NOT_FOUND",
                        "Ruta no encontrada: " + ex.getResourcePath(), HttpStatus.NOT_FOUND.value()));
    }

    // --- Método HTTP no soportado en la ruta (ej: GET en un @PostMapping) ---
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(new ErrorResponse("METHOD_NOT_ALLOWED",
                        "Método no permitido: " + ex.getMessage(), HttpStatus.METHOD_NOT_ALLOWED.value()));
    }

    // --- Genérica ---
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        // dejamos el stack trace en el log: sin esto un 500 es indistinguible de una adivinanza
        log.error("Error interno no manejado", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("INTERNAL_ERROR", "Error interno del servidor", HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }
}