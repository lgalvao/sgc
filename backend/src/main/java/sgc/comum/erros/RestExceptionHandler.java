package sgc.comum.erros;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.validation.*;
import lombok.extern.slf4j.*;
import org.jspecify.annotations.*;
import org.springframework.http.*;
import org.springframework.http.converter.*;
import org.springframework.security.access.*;
import org.springframework.web.bind.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.*;
import org.springframework.web.servlet.mvc.method.annotation.*;
import org.springframework.web.servlet.resource.*;
import sgc.comum.util.*;
import sgc.seguranca.sanitizacao.*;

import java.io.*;
import java.util.*;

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
@NullUnmarked
public class RestExceptionHandler extends ResponseEntityExceptionHandler {
    private static final List<String> PREFIXOS_FRONTEND_EXCLUIDOS = List.of(
            "/api",
            "/actuator",
            "/assets",
            "/swagger-ui",
            "/v3"
    );

    private String sanitizar(@Nullable String texto) {
        return UtilSanitizacao.limparTags(texto);
    }

    private ResponseEntity<@NonNull ErroApi> buildResponseEntity(ErroApi erroApi) {
        return new ResponseEntity<>(erroApi, HttpStatus.valueOf(erroApi.getStatus()));
    }

    @SuppressWarnings("unchecked")
    private ResponseEntity<@NonNull Object> buildResponseEntityObject(ErroApi erroApi) {
        return (ResponseEntity<Object>) (Object) buildResponseEntity(erroApi);
    }

    private String descreverRequisicao(@Nullable WebRequest request) {
        if (request instanceof ServletWebRequest servletWebRequest) {
            var httpRequest = servletWebRequest.getRequest();
            return httpRequest.getMethod() + " " + httpRequest.getRequestURI();
        }
        return "requisição desconhecida";
    }

    private Throwable obterCausaRaiz(Throwable throwable) {
        Throwable atual = throwable;
        while (atual.getCause() != null && atual.getCause() != atual) {
            atual = atual.getCause();
        }
        return atual;
    }

    private String gerarTraceId() {
        return FiltroMonitoramentoHttp.obterCorrelacaoIdAtual();
    }


    /**
     * Trata todas as exceções de negócio que estendem {@link ErroNegocioBase}.
     * O status HTTP e código de erro são definidos na própria exceção.
     */
    @ExceptionHandler(ErroNegocioBase.class)
    protected ResponseEntity<@NonNull ErroApi> handleErroNegocio(ErroNegocioBase ex) {
        String traceId = gerarTraceId();

        if (ex.getStatus().is4xxClientError()) {
            log.warn("[{}] Erro de negócio ({}): {}", traceId, ex.getCode(), ex.getMessage());
        } else {
            log.error("[{}] Erro de negócio crítico ({}): {}", traceId, ex.getCode(), ex.getMessage(), ex);
        }

        ErroApi erroApi = ErroApi.builder()
                .status(ex.getStatus().value())
                .message(sanitizar(ex.getMessage()))
                .code(ex.getCode())
                .traceId(traceId)
                .build();

        if (!ex.getDetails().isEmpty()) {
            erroApi.setDetails(ex.getDetails());
        }

        return buildResponseEntity(erroApi);
    }

    @Override
    protected ResponseEntity<@NonNull Object> handleHttpMessageNotReadable(
            @NonNull HttpMessageNotReadableException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request) {

        log.warn("Erro de mensagem HTTP não legível: {}", ex.getMessage());

        String erro = "Requisição JSON malformada";
        return buildResponseEntityObject(ErroApi.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message(erro)
                .build());
    }

    @Override
    protected ResponseEntity<@NonNull Object> handleHttpMessageNotWritable(
            @NonNull HttpMessageNotWritableException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request) {

        String traceId = gerarTraceId();
        Throwable causaRaiz = obterCausaRaiz(ex);
        String requisicao = descreverRequisicao(request);

        log.error(
                "[{}] Erro ao serializar resposta em {}: {} | Causa raiz: {}: {}",
                traceId,
                requisicao,
                ex.getMessage(),
                causaRaiz.getClass().getSimpleName(),
                causaRaiz.getMessage(),
                ex);

        ErroApi erroApi = ErroApi.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message("Erro inesperado ao processar a resposta.")
                .code("ERRO_SERIALIZACAO")
                .traceId(traceId)
                .build();
        return buildResponseEntityObject(erroApi);
    }

    @Override
    protected ResponseEntity<@NonNull Object> handleMethodArgumentNotValid(
            @NonNull MethodArgumentNotValidException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request) {

        log.warn("Erro de validação de argumento: {}", ex.getMessage());

        String mensagem = "A requisição contém dados de entrada inválidos.";
        var erros = ex.getBindingResult().getFieldErrors().stream().map(
                        erro -> new ErroSubApi(erro.getObjectName(),
                                erro.getField(),
                                sanitizar(erro.getDefaultMessage())))
                .toList();

        erros.forEach(err -> log.info("Falha de validação: campo '{}' - {}", err.campo(), err.mensagem()));

        return buildResponseEntityObject(ErroApi.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message(mensagem)
                .erros(erros)
                .build());
    }

    @Override
    @Nullable
    protected ResponseEntity<@NonNull Object> handleNoResourceFoundException(
            @NonNull NoResourceFoundException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request) {
        if (request instanceof ServletWebRequest servletWebRequest
                && deveEncaminharParaFrontend(ex.getResourcePath())
                && encaminharIndexHtml(servletWebRequest)) {
            return null;
        }

        return super.handleNoResourceFoundException(ex, headers, status, request);
    }

    private boolean deveEncaminharParaFrontend(String caminho) {
        if (caminho.isBlank()) {
            return false;
        }
        String caminhoNormalizado = caminho.startsWith("/") ? caminho : "/" + caminho;
        return !caminho.contains(".")
                && PREFIXOS_FRONTEND_EXCLUIDOS.stream().noneMatch(caminhoNormalizado::startsWith);
    }

    private boolean encaminharIndexHtml(ServletWebRequest servletWebRequest) {
        HttpServletRequest request = servletWebRequest.getRequest();
        HttpServletResponse response = servletWebRequest.getResponse();
        if (response == null || !"GET".equalsIgnoreCase(request.getMethod())) {
            return false;
        }

        try {
            response.setStatus(HttpStatus.OK.value());
            RequestDispatcher requestDispatcher = request.getRequestDispatcher("/index.html");
            requestDispatcher.forward(request, response);
            return true;
        } catch (ServletException | IOException e) {
            log.error("Erro ao encaminhar rota do frontend para index.html: {}", request.getRequestURI(), e);
            return false;
        }
    }

    @ExceptionHandler(ConstraintViolationException.class)
    protected ResponseEntity<@NonNull ErroApi> handleConstraintViolationException(
            ConstraintViolationException ex) {
        String traceId = gerarTraceId();
        log.error("[{}] Erro de constraint de banco de dados: {}", traceId, ex.getMessage(), ex);
        String mensagem = "A requisição contém dados inválidos.";
        var erros = ex.getConstraintViolations().stream().map(violation -> new ErroSubApi(
                        violation.getRootBeanClass().getSimpleName(),
                        violation.getPropertyPath().toString(),
                        sanitizar(violation.getMessage())))
                .toList();

        ErroApi erroApi = ErroApi.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message(mensagem)
                .erros(erros)
                .traceId(traceId)
                .build();
        return buildResponseEntity(erroApi);
    }

    @ExceptionHandler(AccessDeniedException.class)
    protected ResponseEntity<@NonNull ErroApi> handleAccessDenied(AccessDeniedException ex) {
        log.debug("Acesso negado via Spring Security: {}", ex.getMessage());
        ErroApi erroApi = ErroApi.builder()
                .status(HttpStatus.FORBIDDEN.value())
                .message("Acesso negado.")
                .build();
        return buildResponseEntity(erroApi);
    }

    /**
     * Trata erros internos do sistema que indicam bugs ou problemas de
     * configuração.
     * Estes erros nunca deveriam ocorrer em produção se o sistema está funcionando
     * corretamente.
     */
    @ExceptionHandler(ErroInterno.class)
    protected ResponseEntity<@NonNull ErroApi> handleErroInterno(ErroInterno ex) {
        String traceId = gerarTraceId();
        log.error("[{}] ERRO INTERNO DO SISTEMA - BUG DETECTADO: {}",
                traceId, ex.getMessage(), ex);

        ErroApi erroApi = ErroApi.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message("Erro interno do sistema.")
                .code("ERRO_INTERNO")
                .traceId(traceId)
                .build();
        return buildResponseEntity(erroApi);
    }

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<@NonNull ErroApi> handleGenericException(Exception ex) {
        String traceId = gerarTraceId();
        log.error("[{}] ERRO NÃO TRATADO DETECTADO: {}", traceId, ex.getMessage(), ex);

        ErroApi erroApi = ErroApi.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message("Erro inesperado.")
                .code("ERRO_INTERNO")
                .traceId(traceId)
                .build();
        return buildResponseEntity(erroApi);
    }
}
