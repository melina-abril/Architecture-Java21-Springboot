package com.techforb.app.shared.error;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Todos los errores salen como ProblemDetail (RFC 9457): title, status, detail, code y, si aplica,
 * errors = { campo: [mensajes] }. Es la forma que espera el front. Los controllers y services NO
 * atrapan excepciones: lanzan NotFoundException, ConflictException o BusinessRuleException.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ProblemDetail> notFound(NotFoundException exception) {
        return problem(HttpStatus.NOT_FOUND, "NOT_FOUND", exception.getMessage(), Map.of());
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ProblemDetail> conflict(ConflictException exception) {
        return problem(HttpStatus.CONFLICT, "CONFLICT", exception.getMessage(), exception.fieldErrors());
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ProblemDetail> businessRule(BusinessRuleException exception) {
        return problem(HttpStatus.UNPROCESSABLE_ENTITY, exception.getCode(), exception.getMessage(), Map.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> validation(MethodArgumentNotValidException exception) {
        Map<String, List<String>> errors = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(error -> errors
                .computeIfAbsent(error.getField(), field -> new ArrayList<>())
                .add(error.getDefaultMessage() == null ? "Valor inválido" : error.getDefaultMessage()));
        return problem(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Revisa los datos ingresados.", errors);
    }

    /** JSON mal formado o un valor que no entra en su tipo (ej. un enum que no existe). */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> unreadableBody(HttpMessageNotReadableException exception) {
        return problem(HttpStatus.BAD_REQUEST, "INVALID_BODY", "El cuerpo de la solicitud no es válido.", Map.of());
    }

    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ProblemDetail> invalidArgument(Exception exception) {
        return problem(HttpStatus.BAD_REQUEST, "INVALID_ARGUMENT", "Algún parámetro no es válido.", Map.of());
    }

    private ResponseEntity<ProblemDetail> problem(HttpStatus status, String code, String message, Map<String, List<String>> errors) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(status, message == null ? status.getReasonPhrase() : message);
        detail.setTitle(status.getReasonPhrase());
        detail.setProperty("code", code);
        if (!errors.isEmpty()) {
            detail.setProperty("errors", errors);
        }
        return ResponseEntity.status(status).body(detail);
    }
}
