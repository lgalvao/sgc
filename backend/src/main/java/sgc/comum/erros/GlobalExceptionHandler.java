package sgc.comum.erros;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.validation.FieldError;


import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import sgc.processo.modelo.ErroProcesso;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ErroValidacao.class)
    public ResponseEntity<Object> handleErroValidacao(ErroValidacao ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.UNPROCESSABLE_ENTITY.value());
        body.put("error", "Unprocessable Entity");
        body.put("message", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolationException(ConstraintViolationException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Bad Request");
        body.put("message", "Validation failed");
        body.put("errors", ex.getConstraintViolations().stream()
                .map(violation -> {
                    Map<String, String> error = new HashMap<>();
                    error.put("field", violation.getPropertyPath().toString());
                    error.put("defaultMessage", violation.getMessage());
                    return error;
                })
                .toList());

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    // You can add more exception handlers here for other custom exceptions
    // For example, to handle ErroEntidadeNaoEncontrada globally:
    @ExceptionHandler(ErroEntidadeNaoEncontrada.class)
    public ResponseEntity<Object> handleErroEntidadeNaoEncontrada(ErroEntidadeNaoEncontrada ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.NOT_FOUND.value());
        body.put("error", "Not Found");
        body.put("message", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    // To handle ErroDominioAccessoNegado globally:
    @ExceptionHandler(ErroDominioAccessoNegado.class)
    public ResponseEntity<Object> handleErroDominioAccessoNegado(ErroDominioAccessoNegado ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.FORBIDDEN.value());
        body.put("error", "Forbidden");
        body.put("message", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.FORBIDDEN);
    }

    // To handle IllegalStateException globally:
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Object> handleIllegalStateException(IllegalStateException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Bad Request");
        body.put("erro", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    // To handle IllegalArgumentException globally:
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Bad Request");
        body.put("message", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    // To handle ErroProcesso globally:
    @ExceptionHandler(ErroProcesso.class)
    public ResponseEntity<Object> handleErroProcesso(ErroProcesso ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.UNPROCESSABLE_ENTITY.value());
        body.put("erro", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Bad Request");
        body.put("message", "Validation failed");
        body.put("errors", ex.getBindingResult().getAllErrors().stream()
            .map(error -> {
                Map<String, String> errorDetails = new HashMap<>();
                if (error instanceof FieldError) {
                    errorDetails.put("field", ((FieldError) error).getField());
                } else {
                    errorDetails.put("object", error.getObjectName());
                }
                errorDetails.put("defaultMessage", error.getDefaultMessage());
                return errorDetails;
            })
            .toList());

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }
}