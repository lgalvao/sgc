package sgc.comum.erros;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Handler centralizado para tratamento de exceções REST.
 *
 * <p>Todas as exceções de negócio devem estender {@link ErroNegocioBase} para serem
 * tratadas automaticamente pelo método {@link #handleErroNegocio(ErroNegocioBase)}.
 */
@Slf4j
@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {
    private static final PolicyFactory SANITIZER_POLICY = new HtmlPolicyBuilder().toFactory();

    private String sanitizar(String texto) {
        return texto == null ? null : SANITIZER_POLICY.sanitize(texto);
    }

    private ResponseEntity<Object> buildResponseEntity(ErroApi erroApi) {
        return new ResponseEntity<>(erroApi, HttpStatus.valueOf(erroApi.getStatus()));
    }

    /**
     * Trata todas as exceções de negócio que estendem {@link ErroNegocioBase}.
     * O status HTTP e código de erro são definidos na própria exceção.
     */
    @ExceptionHandler(ErroNegocioBase.class)
    protected ResponseEntity<Object> handleErroNegocio(ErroNegocioBase ex) {
        String traceId = UUID.randomUUID().toString();

        if (ex.getStatus().is4xxClientError()) {
            log.warn("Erro de negócio ({}): {}", ex.getCode(), ex.getMessage());
        } else {
            log.error("Erro de negócio ({}): {}", ex.getCode(), ex.getMessage(), ex);
        }

        ErroApi erroApi = new ErroApi(
            ex.getStatus(),
            sanitizar(ex.getMessage()),
            ex.getCode(),
            traceId
        );

        if (ex.getDetails() != null && !ex.getDetails().isEmpty()) {
            erroApi.setDetails(ex.getDetails());
        }

        return buildResponseEntity(erroApi);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            @Nullable HttpMessageNotReadableException ex,
            @Nullable HttpHeaders headers,
            @Nullable HttpStatusCode status,
            @Nullable WebRequest request) {
        log.warn("Erro de mensagem HTTP não legível: {}", ex != null ? ex.getMessage() : "sem detalhe");
        String error = "Requisição JSON malformada";
        return buildResponseEntity(new ErroApi(HttpStatus.BAD_REQUEST, error));
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            @Nullable MethodArgumentNotValidException ex,
            @Nullable HttpHeaders headers,
            @Nullable HttpStatusCode status,
            @Nullable WebRequest request) {

        log.info("Erro de validação de argumento");
        log.debug(">> Detalhes do erro: {}", ex != null ? ex.getMessage() : null);

        String message = "A requisição contém dados de entrada inválidos.";
        var subErrors = ex != null
                ? ex.getBindingResult().getFieldErrors().stream().map(
                        error -> new ErroSubApi(error.getObjectName(),
                                error.getField(),
                                null, // SENTINEL: Prevent sensitive data leak
                                sanitizar(error.getDefaultMessage())))
                .toList() : null;

        return buildResponseEntity(new ErroApi(HttpStatus.BAD_REQUEST, message, subErrors));
    }

    @ExceptionHandler(ErroValidacao.class)
    protected ResponseEntity<Object> handleErroValidacao(ErroValidacao ex) {
        log.warn("Erro de validação de negócio: {}", ex.getMessage());
        ErroApi erroApi = new ErroApi(HttpStatus.UNPROCESSABLE_CONTENT, sanitizar(ex.getMessage()));
        if (ex.getDetails() != null && !ex.getDetails().isEmpty()) {
            log.warn("Detalhes da validação: {}", ex.getDetails());
            erroApi.setDetails(ex.getDetails());
        }
        return buildResponseEntity(erroApi);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    protected ResponseEntity<Object> handleConstraintViolationException(
            ConstraintViolationException ex) {
        String traceId = UUID.randomUUID().toString();
        log.error("[{}] Erro de constraint de banco de dados: {}", traceId, ex.getMessage(), ex);
        String message = "A requisição contém dados inválidos.";
        var subErrors =
                ex.getConstraintViolations().stream().map(violation ->
                                new ErroSubApi(
                                        violation.getRootBeanClass().getSimpleName(),
                                        violation.getPropertyPath().toString(),
                                        null, // SENTINEL: Prevent sensitive data leak
                                        sanitizar(violation.getMessage())))
                        .collect(Collectors.toList());

        ErroApi erroApi = new ErroApi(HttpStatus.BAD_REQUEST, message, subErrors);
        erroApi.setTraceId(traceId);
        return buildResponseEntity(erroApi);
    }

    @ExceptionHandler(AccessDeniedException.class)
    protected ResponseEntity<Object> handleAccessDenied(AccessDeniedException ex) {
        log.warn("Acesso negado via Spring Security: {}", ex.getMessage());
        ErroApi erroApi = new ErroApi(HttpStatus.FORBIDDEN, "Acesso negado.");
        return buildResponseEntity(erroApi);
    }

    @ExceptionHandler(ErroAutenticacao.class)
    protected ResponseEntity<Object> handleErroAutenticacao(ErroAutenticacao ex) {
        log.warn("Erro de autenticação: {}", ex.getMessage());
        return buildResponseEntity(new ErroApi(HttpStatus.UNAUTHORIZED, sanitizar(ex.getMessage()), "NAO_AUTORIZADO", UUID.randomUUID().toString()));
    }

    @ExceptionHandler(IllegalStateException.class)
    protected ResponseEntity<Object> handleIllegalStateException(IllegalStateException ex) {
        String traceId = UUID.randomUUID().toString();
        log.warn("[{}] Estado ilegal da aplicação: {}", traceId, ex.getMessage());
        String message = "A operação não pode ser executada no estado atual do recurso.";
        return buildResponseEntity(new ErroApi(HttpStatus.CONFLICT, message, "ESTADO_ILEGAL", traceId));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    protected ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException ex) {
        String traceId = UUID.randomUUID().toString();
        log.error("[{}] Argumento ilegal fornecido: {}", traceId, ex.getMessage(), ex);
        String message = "A requisição contém um argumento inválido ou malformado.";
        return buildResponseEntity(new ErroApi(HttpStatus.BAD_REQUEST, message, "ARGUMENTO_INVALIDO", traceId));
    }

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<Object> handleGenericException(Exception ex) {
        String traceId = UUID.randomUUID().toString();
        log.error("[{}] Erro inesperado na aplicação", traceId, ex);
        return buildResponseEntity(
                new ErroApi(HttpStatus.INTERNAL_SERVER_ERROR, "Erro inesperado", "ERRO_INTERNO", traceId));
    }
}
