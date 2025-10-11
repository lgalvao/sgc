package sgc.comum.erros;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import sgc.processo.modelo.ErroProcesso;

import java.util.stream.Collectors;

@Slf4j
@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    private static final PolicyFactory SANITIZER_POLICY = Sanitizers.FORMATTING.and(Sanitizers.LINKS);

    private String sanitize(String untrustedHtml) {
        if (untrustedHtml == null) {
            return null;
        }
        return SANITIZER_POLICY.sanitize(untrustedHtml);
    }

    private ResponseEntity<Object> buildResponseEntity(ApiError apiError) {
        return new ResponseEntity<>(apiError, HttpStatus.valueOf(apiError.getStatus()));
    }


    protected ResponseEntity<Object> handleHttpMessageNotReadable(
        HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        String error = "Requisição JSON malformada";
        return buildResponseEntity(new ApiError(HttpStatus.BAD_REQUEST, error));
    }


    protected ResponseEntity<Object> handleMethodArgumentNotValid(
        MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.warn("Erro de validação de argumento de método: {}", ex.getMessage());
        String message = "A requisição contém dados de entrada inválidos.";
        var subErrors = ex.getBindingResult().getFieldErrors().stream()
            .map(error -> new ApiSubError(
                error.getObjectName(),
                error.getField(),
                error.getRejectedValue(),
                sanitize(error.getDefaultMessage())))
            .collect(Collectors.toList());
        return buildResponseEntity(new ApiError(HttpStatus.BAD_REQUEST, message, subErrors));
    }

    @ExceptionHandler(ErroValidacao.class)
    protected ResponseEntity<Object> handleErroValidacao(ErroValidacao ex) {
        log.warn("Erro de validação de negócio: {}", ex.getMessage());
        return buildResponseEntity(new ApiError(HttpStatus.UNPROCESSABLE_ENTITY, sanitize(ex.getMessage())));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    protected ResponseEntity<Object> handleConstraintViolationException(ConstraintViolationException ex) {
        log.error("Erro de constraint de banco de dados: {}", ex.getMessage(), ex);
        String message = "A requisição contém dados inválidos.";
        var subErrors = ex.getConstraintViolations().stream()
            .map(violation -> new ApiSubError(
                violation.getRootBeanClass().getSimpleName(),
                violation.getPropertyPath().toString(),
                violation.getInvalidValue(),
                sanitize(violation.getMessage())))
            .collect(Collectors.toList());
        return buildResponseEntity(new ApiError(HttpStatus.BAD_REQUEST, message, subErrors));
    }

    @ExceptionHandler(ErroEntidadeNaoEncontrada.class)
    protected ResponseEntity<Object> handleErroEntidadeNaoEncontrada(ErroEntidadeNaoEncontrada ex) {
        log.warn("Entidade não encontrada: {}", ex.getMessage());
        return buildResponseEntity(new ApiError(HttpStatus.NOT_FOUND, sanitize(ex.getMessage())));
    }

    @ExceptionHandler(ErroDominioAccessoNegado.class)
    protected ResponseEntity<Object> handleErroDominioAccessoNegado(ErroDominioAccessoNegado ex) {
        log.warn("Acesso negado: {}", ex.getMessage());
        return buildResponseEntity(new ApiError(HttpStatus.FORBIDDEN, sanitize(ex.getMessage())));
    }

    @ExceptionHandler(IllegalStateException.class)
    protected ResponseEntity<Object> handleIllegalStateException(IllegalStateException ex) {
        log.error("Estado ilegal da aplicação: {}", ex.getMessage(), ex);
        String message = "Ocorreu um erro de estado na aplicação. Contate o suporte.";
        return buildResponseEntity(new ApiError(HttpStatus.BAD_REQUEST, message));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    protected ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.error("Argumento ilegal fornecido: {}", ex.getMessage(), ex);
        String message = "Foi fornecido um argumento ilegal. Contate o suporte.";
        return buildResponseEntity(new ApiError(HttpStatus.BAD_REQUEST, message));
    }

    @ExceptionHandler(ErroProcesso.class)
    protected ResponseEntity<Object> handleErroProcesso(ErroProcesso ex) {
        log.error("Erro de negócio no processo: {}", ex.getMessage(), ex);
        return buildResponseEntity(new ApiError(HttpStatus.UNPROCESSABLE_ENTITY, sanitize(ex.getMessage())));
    }

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<Object> handleGenericException(Exception ex) {
        log.error("Erro inesperado na aplicação: {}", ex.getMessage(), ex);
        String message = String.format(
            "Ocorreu um erro inesperado (%s). Contate o suporte.",
            ex.getClass().getSimpleName()
        );
        return buildResponseEntity(new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, message));
    }
}