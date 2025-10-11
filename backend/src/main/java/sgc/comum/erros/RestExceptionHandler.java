package sgc.comum.erros;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.validation.FieldError;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import sgc.processo.modelo.ErroProcesso;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@ControllerAdvice
public class RestExceptionHandler {

    private static final PolicyFactory SANITIZER_POLICY = Sanitizers.FORMATTING.and(Sanitizers.LINKS);

    private String sanitize(String untrustedHtml) {
        if (untrustedHtml == null) {
            return null;
        }
        return SANITIZER_POLICY.sanitize(untrustedHtml);
    }

    @ExceptionHandler(ErroValidacao.class)
    public ResponseEntity<Object> handleErroValidacao(ErroValidacao ex) {
        log.warn("Erro de validação de negócio: {}", ex.getMessage());
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.UNPROCESSABLE_ENTITY.value());
        body.put("error", "Unprocessable Entity");
        body.put("message", sanitize(ex.getMessage()));
        if (ex.getDetails() != null) {
            body.put("details", ex.getDetails());
        }
        return new ResponseEntity<>(body, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolationException(ConstraintViolationException ex) {
        log.error("Erro de constraint de banco de dados: {}", ex.getMessage(), ex);
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Bad Request");
        body.put("message", "A requisição contém dados inválidos.");
        body.put("errors", ex.getConstraintViolations().stream()
                .map(violation -> {
                    Map<String, String> error = new HashMap<>();
                    error.put("field", violation.getPropertyPath().toString());
                    error.put("defaultMessage", sanitize(violation.getMessage()));
                    return error;
                })
                .toList());

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ErroEntidadeNaoEncontrada.class)
    public ResponseEntity<Object> handleErroEntidadeNaoEncontrada(ErroEntidadeNaoEncontrada ex) {
        log.warn("Entidade não encontrada: {}", ex.getMessage());
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.NOT_FOUND.value());
        body.put("error", "Not Found");
        body.put("message", sanitize(ex.getMessage()));
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ErroDominioAccessoNegado.class)
    public ResponseEntity<Object> handleErroDominioAccessoNegado(ErroDominioAccessoNegado ex) {
        log.warn("Acesso negado: {}", ex.getMessage());
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.FORBIDDEN.value());
        body.put("error", "Forbidden");
        body.put("message", sanitize(ex.getMessage()));
        return new ResponseEntity<>(body, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Object> handleIllegalStateException(IllegalStateException ex) {
        log.error("Estado ilegal da aplicação: {}", ex.getMessage(), ex);
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Bad Request");
        body.put("message", sanitize(ex.getMessage()));
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.error("Argumento ilegal fornecido: {}", ex.getMessage(), ex);
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Bad Request");
        body.put("message", sanitize(ex.getMessage()));
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ErroProcesso.class)
    public ResponseEntity<Object> handleErroProcesso(ErroProcesso ex) {
        log.error("Erro de negócio no processo: {}", ex.getMessage(), ex);
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.UNPROCESSABLE_ENTITY.value());
        body.put("error", "Unprocessable Entity");
        body.put("message", sanitize(ex.getMessage()));
        return new ResponseEntity<>(body, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        log.warn("Erro de validação de argumento de método: {}", ex.getMessage());
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Bad Request");
        body.put("message", "A requisição contém dados de entrada inválidos.");
        body.put("errors", ex.getBindingResult().getAllErrors().stream()
            .map(error -> {
                Map<String, String> errorDetails = new HashMap<>();
                if (error instanceof FieldError) {
                    errorDetails.put("field", ((FieldError) error).getField());
                } else {
                    errorDetails.put("object", error.getObjectName());
                }
                errorDetails.put("defaultMessage", sanitize(error.getDefaultMessage()));
                return errorDetails;
            })
            .toList());

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGenericException(Exception ex) {
        log.error("Erro inesperado na aplicação: {}", ex.getMessage(), ex);
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("error", "Internal Server Error");
        body.put("message", "Ocorreu um erro inesperado. Contate o suporte.");
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}