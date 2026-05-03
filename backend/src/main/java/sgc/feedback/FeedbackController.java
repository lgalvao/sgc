package sgc.feedback;

import jakarta.validation.*;
import lombok.*;
import org.jspecify.annotations.*;
import org.springframework.context.annotation.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.*;
import sgc.feedback.dto.*;
import tools.jackson.databind.*;

import java.io.*;

/**
 * Endpoint de feedback disponível apenas no perfil {@code hom} (UAT).
 *
 * <p>Com {@code spring.profiles.active=prod} este bean não é instanciado
 * e o endpoint retorna 404 para qualquer sonda.
 */
@RestController
@RequestMapping("/api/feedback")
@Profile("hom")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class FeedbackController {

    private final FeedbackService feedbackService;
    private final ObjectMapper objectMapper;

    /**
     * Registra um novo feedback enviado pelo widget UAT.
     *
     * @param data       payload JSON serializado (campo {@code data} do formulário multipart)
     * @param screenshot captura de tela opcional (campo {@code screenshot})
     * @return 201 Created com id e data/hora do registro
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FeedbackRespostaDto> registrar(
            @RequestPart("data") String data,
            @RequestPart(value = "screenshot", required = false) @Nullable MultipartFile screenshot
    ) {
        FeedbackPayloadDto payload;
        try {
            payload = objectMapper.readValue(data, FeedbackPayloadDto.class);
        } catch (tools.jackson.core.JacksonException e) {
            throw new sgc.comum.erros.ErroValidacao("VALIDATION_ERROR", "payload inválido: " + e.getMessage());
        }
        validar(payload);
        FeedbackRespostaDto resposta = feedbackService.registrar(payload, screenshot);
        return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
    }

    private void validar(FeedbackPayloadDto payload) {
        var violations = obterViolacoes(payload);
        if (!violations.isEmpty()) {
            var primeira = violations.iterator().next();
            throw new sgc.comum.erros.ErroValidacao(
                    "VALIDATION_ERROR",
                    primeira.getPropertyPath() + " " + primeira.getMessage()
            );
        }
    }

    private java.util.Set<ConstraintViolation<FeedbackPayloadDto>> obterViolacoes(FeedbackPayloadDto payload) {
        var factory = jakarta.validation.Validation.buildDefaultValidatorFactory();
        var validator = factory.getValidator();
        return validator.validate(payload);
    }
}
