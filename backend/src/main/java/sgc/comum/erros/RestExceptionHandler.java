package sgc.comum.erros;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.lang.Nullable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import sgc.processo.model.ErroProcesso;

import java.util.stream.Collectors;

@Slf4j
@ControllerAdvice
// TODO essa classe me parece muito repetitiva. E os tratamentos não estão específicos o suficiente.
public class RestExceptionHandler extends ResponseEntityExceptionHandler {
    private static final PolicyFactory SANITIZER_POLICY = new HtmlPolicyBuilder().toFactory();

    private String sanitizar(String texto) {
        return texto == null ? null : SANITIZER_POLICY.sanitize(texto);
    }

    private ResponseEntity<Object> buildResponseEntity(ErroApi erroApi) {
        return new ResponseEntity<>(erroApi, HttpStatus.valueOf(erroApi.getStatus()));
    }

    protected ResponseEntity<Object> handleHttpMessageNotReadable(
        @Nullable HttpMessageNotReadableException ex, @Nullable HttpHeaders headers, @Nullable HttpStatusCode status, @Nullable WebRequest request) {
        String error = "Requisição JSON malformada";
        return buildResponseEntity(new ErroApi(HttpStatus.BAD_REQUEST, error));
    }

    protected ResponseEntity<Object> handleMethodArgumentNotValid(
        @Nullable MethodArgumentNotValidException ex, @Nullable HttpHeaders headers, @Nullable HttpStatusCode status, @Nullable WebRequest request) {
        log.warn("Erro de validação de argumento de método: {}", ex != null ? ex.getMessage() : null);
        String message = "A requisição contém dados de entrada inválidos.";
        var subErrors = ex != null ? ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new ErroSubApi(
                        error.getObjectName(),
                        error.getField(),
                        error.getRejectedValue(),
                        sanitizar(error.getDefaultMessage())))
                .collect(Collectors.toList()) : null;
        return buildResponseEntity(new ErroApi(HttpStatus.BAD_REQUEST, message, subErrors));
    }

    @ExceptionHandler(ErroValidacao.class)
    protected ResponseEntity<Object> handleErroValidacao(ErroValidacao ex) {
        log.warn("Erro de validação de negócio: {}", ex.getMessage());
        ErroApi erroApi = new ErroApi(HttpStatus.UNPROCESSABLE_ENTITY, sanitizar(ex.getMessage()));
        if (ex.getDetails() != null && !ex.getDetails().isEmpty()) {
            erroApi.setDetails(ex.getDetails());
        }
        return buildResponseEntity(erroApi);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    protected ResponseEntity<Object> handleConstraintViolationException(ConstraintViolationException ex) {
        log.error("Erro de constraint de banco de dados: {}", ex.getMessage(), ex);
        String message = "A requisição contém dados inválidos.";
        var subErrors = ex.getConstraintViolations().stream()
            .map(violation -> new ErroSubApi(
                violation.getRootBeanClass().getSimpleName(),
                violation.getPropertyPath().toString(),
                violation.getInvalidValue(),
                sanitizar(violation.getMessage())))
            .collect(Collectors.toList());
        return buildResponseEntity(new ErroApi(HttpStatus.BAD_REQUEST, message, subErrors));
    }

    @ExceptionHandler(ErroEntidadeNaoEncontrada.class)
    protected ResponseEntity<Object> handleErroDominioNaoEncontrado(ErroEntidadeNaoEncontrada ex) {
        log.warn("Entidade não encontrada: {}", ex.getMessage());
        return buildResponseEntity(new ErroApi(HttpStatus.NOT_FOUND, sanitizar(ex.getMessage())));
    }

    @ExceptionHandler(ErroAccessoNegado.class)
    protected ResponseEntity<Object> handleErroDominioAccessoNegado(ErroAccessoNegado ex) {
        log.warn("Acesso negado: {}", ex.getMessage());
        return buildResponseEntity(new ErroApi(HttpStatus.FORBIDDEN, sanitizar(ex.getMessage())));
    }

    @ExceptionHandler(AccessDeniedException.class)
    protected ResponseEntity<Object> handleAccessDenied(AccessDeniedException ex) {
        log.warn("Acesso negado via Spring Security: {}", ex.getMessage());
        ErroApi erroApi = new ErroApi(HttpStatus.FORBIDDEN, "Acesso negado.");
        return buildResponseEntity(erroApi);
    }

    @ExceptionHandler(IllegalStateException.class)
    protected ResponseEntity<Object> handleIllegalStateException(IllegalStateException ex) {
        log.error("Estado ilegal da aplicação: {}", ex.getMessage(), ex);
        String message = "A operação não pode ser executada no estado atual do recurso.";
        return buildResponseEntity(new ErroApi(HttpStatus.CONFLICT, message));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    protected ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.error("Argumento ilegal fornecido: {}", ex.getMessage(), ex);
        String message = "A requisição contém um argumento inválido ou malformado.";
        return buildResponseEntity(new ErroApi(HttpStatus.BAD_REQUEST, message));
    }

    @ExceptionHandler(ErroProcesso.class)
    protected ResponseEntity<Object> handleErroProcesso(ErroProcesso ex) {
        log.error("Erro de negócio no processo: {}", ex.getMessage(), ex);
        return buildResponseEntity(new ErroApi(HttpStatus.CONFLICT, sanitizar(ex.getMessage())));
    }

    @ExceptionHandler(ErroNegocio.class)
    protected ResponseEntity<Object> handleErroNegocio(ErroNegocio ex) {
        log.warn("Erro de negócio: {}", ex.getMessage());
        return buildResponseEntity(new ErroApi(HttpStatus.UNPROCESSABLE_ENTITY, sanitizar(ex.getMessage())));
    }

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<Object> handleGenericException(Exception ex) {
        log.error("Erro inesperado na aplicação: {}", ex.getMessage(), ex);
        return buildResponseEntity(new ErroApi(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage()));
    }
}