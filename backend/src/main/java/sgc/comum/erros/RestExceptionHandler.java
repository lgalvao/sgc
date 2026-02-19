package sgc.comum.erros;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
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
import sgc.seguranca.sanitizacao.UtilSanitizacao;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.UUID;

/**
 * Handler centralizado para tratamento de exceções REST.
 *
 * <p>
 * Todas as exceções de negócio devem estender {@link ErroNegocioBase} para
 * serem
 * tratadas automaticamente pelo método
 * {@link #handleErroNegocio(ErroNegocioBase)}.
 */
@Slf4j
@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    private String sanitizar(String texto) {
        return UtilSanitizacao.sanitizar(texto);
    }

    private ResponseEntity<ErroApi> buildResponseEntity(ErroApi erroApi) {
        return new ResponseEntity<>(erroApi, HttpStatus.valueOf(erroApi.getStatus()));
    }

    @SuppressWarnings("unchecked")
    private ResponseEntity<Object> buildResponseEntityObject(ErroApi erroApi) {
        return (ResponseEntity<Object>) (Object) buildResponseEntity(erroApi);
    }

    private String getStackTrace(Throwable ex) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        return sw.toString();
    }

    /**
     * Trata todas as exceções de negócio que estendem {@link ErroNegocioBase}.
     * O status HTTP e código de erro são definidos na própria exceção.
     */
    @ExceptionHandler(ErroNegocioBase.class)
    protected ResponseEntity<ErroApi> handleErroNegocio(ErroNegocioBase ex) {
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
                traceId);
        erroApi.setStackTrace(getStackTrace(ex));

        if (!ex.getDetails().isEmpty()) {
            erroApi.setDetails(ex.getDetails());
        }

        return buildResponseEntity(erroApi);
    }

    @Override
    protected @Nullable ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            @Nullable HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        log.warn("Erro de mensagem HTTP não legível: {}", ex.getMessage());
        String error = "Requisição JSON malformada";
        return buildResponseEntityObject(new ErroApi(HttpStatus.BAD_REQUEST, error));
    }

    @Override
    protected @Nullable ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            @Nullable HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        log.warn("Erro de validação de argumento", ex);

        String message = "A requisição contém dados de entrada inválidos.";
        var subErrors = ex.getBindingResult().getFieldErrors().stream().map(
                        error -> new ErroSubApi(error.getObjectName(),
                                error.getField(),
                                sanitizar(error.getDefaultMessage())))
                .toList();

        subErrors.forEach(err -> log.info("Falha de validação: campo '{}' - {}", err.field(), err.message()));

        return buildResponseEntityObject(new ErroApi(HttpStatus.BAD_REQUEST, message, subErrors));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    protected ResponseEntity<ErroApi> handleConstraintViolationException(
            ConstraintViolationException ex) {
        String traceId = UUID.randomUUID().toString();
        log.error("[{}] Erro de constraint de banco de dados: {}", traceId, ex.getMessage(), ex);
        String message = "A requisição contém dados inválidos.";
        var subErrors = ex.getConstraintViolations().stream().map(violation -> new ErroSubApi(
                        violation.getRootBeanClass().getSimpleName(),
                        violation.getPropertyPath().toString(),
                        sanitizar(violation.getMessage())))
                .toList();

        ErroApi erroApi = new ErroApi(HttpStatus.BAD_REQUEST, message, subErrors);
        erroApi.setTraceId(traceId);
        return buildResponseEntity(erroApi);
    }

    @ExceptionHandler(AccessDeniedException.class)
    protected ResponseEntity<ErroApi> handleAccessDenied(AccessDeniedException ex) {
        log.debug("Acesso negado via Spring Security: {}", ex.getMessage());
        ErroApi erroApi = new ErroApi(HttpStatus.FORBIDDEN, "ACESSO NEGADO: " + ex.getMessage());
        return buildResponseEntity(erroApi);
    }

    @ExceptionHandler(ErroAutenticacao.class)
    protected ResponseEntity<ErroApi> handleErroAutenticacao(ErroAutenticacao ex) {
        log.warn("Erro de autenticação: {}", ex.getMessage());
        return buildResponseEntity(new ErroApi(HttpStatus.UNAUTHORIZED, sanitizar(ex.getMessage()), "NAO_AUTORIZADO",
                UUID.randomUUID().toString()));
    }

    /**
     * Trata erros internos do sistema que indicam bugs ou problemas de
     * configuração.
     * Estes erros nunca deveriam ocorrer em produção se o sistema está funcionando
     * corretamente.
     */
    @ExceptionHandler(ErroInterno.class)
    protected ResponseEntity<ErroApi> handleErroInterno(ErroInterno ex) {
        String traceId = UUID.randomUUID().toString();
        log.error("[{}] ERRO INTERNO - Isso indica um bug que precisa ser corrigido: {}",
                traceId, ex.getMessage(), ex);

        String mensagemUsuario = "ERRO INTERNO: " + ex.getMessage();
        ErroApi erroApi = new ErroApi(HttpStatus.INTERNAL_SERVER_ERROR, mensagemUsuario, "ERRO_INTERNO", traceId);
        erroApi.setStackTrace(getStackTrace(ex));
        return buildResponseEntity(erroApi);
    }

    @ExceptionHandler(IllegalStateException.class)
    protected ResponseEntity<ErroApi> handleIllegalStateException(IllegalStateException ex) {
        String traceId = UUID.randomUUID().toString();
        log.warn("[{}] Estado ilegal da aplicação: {}", traceId, ex.getMessage());
        String message = "ESTADO ILEGAL: " + ex.getMessage();
        ErroApi erroApi = new ErroApi(HttpStatus.CONFLICT, message, "ESTADO_ILEGAL", traceId);
        erroApi.setStackTrace(getStackTrace(ex));
        return buildResponseEntity(erroApi);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    protected ResponseEntity<ErroApi> handleIllegalArgumentException(IllegalArgumentException ex) {
        String traceId = UUID.randomUUID().toString();
        log.error("[{}] Argumento ilegal fornecido: {}", traceId, ex.getMessage(), ex);
        String message = "ARGUMENTO INVÁLIDO: " + ex.getMessage();
        ErroApi erroApi = new ErroApi(HttpStatus.BAD_REQUEST, message, "ARGUMENTO_INVALIDO", traceId);
        erroApi.setStackTrace(getStackTrace(ex));
        return buildResponseEntity(erroApi);
    }

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ErroApi> handleGenericException(Exception ex) {
        String traceId = UUID.randomUUID().toString();
        log.error("[{}] Erro inesperado na aplicação", traceId, ex);
        String message = "ERRO INESPERADO: " + (ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName());
        ErroApi erroApi = new ErroApi(HttpStatus.INTERNAL_SERVER_ERROR, message, "ERRO_INTERNO", traceId);
        erroApi.setStackTrace(getStackTrace(ex));
        return buildResponseEntity(erroApi);
    }
}
