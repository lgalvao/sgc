package sgc.feedback;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sgc.comum.erros.ErroValidacao;
import sgc.feedback.dto.FeedbackListagemDto;
import sgc.feedback.dto.FeedbackPayloadDto;
import sgc.feedback.dto.FeedbackRespostaDto;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Endpoint de feedback disponível apenas no perfil {@code hom}.
 *
 * <p>Com {@code spring.profiles.active=prod} este bean não é instanciado
 * e o endpoint retorna 404 para qualquer sonda.
 */
@RestController
@RequestMapping("/api/feedback")
@Profile({"hom", "e2e", "test"})
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class FeedbackController {

    private final FeedbackService feedbackService;
    private final ObjectMapper objectMapper;

    /**
     * Registra um novo feedback enviado pelo widget.
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
        } catch (JacksonException e) {
            throw new ErroValidacao("ERRO_VALIDACAO", "payload inválido: " + e.getMessage());
        }
        validar(payload);
        FeedbackRespostaDto resposta = feedbackService.registrar(payload, screenshot);
        return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
    }

    @GetMapping("/listar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<FeedbackListagemDto>> listar(
            @RequestParam(defaultValue = "100") int limite
    ) {
        return ResponseEntity.ok(feedbackService.listarRecentes(limite));
    }

    @GetMapping("/{codigo}/screenshot")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> exibirScreenshot(@PathVariable UUID codigo) {
        byte[] imagem = feedbackService.obterScreenshot(codigo);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("image/webp"))
                .body(imagem);
    }

    private void validar(FeedbackPayloadDto payload) {
        var violations = obterViolacoes(payload);
        if (!violations.isEmpty()) {
            var primeira = violations.iterator().next();
            throw new ErroValidacao(
                    "ERRO_VALIDACAO",
                    primeira.getPropertyPath() + " " + primeira.getMessage()
            );
        }
    }

    private Set<ConstraintViolation<FeedbackPayloadDto>> obterViolacoes(FeedbackPayloadDto payload) {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var validator = factory.getValidator();
            return validator.validate(payload);
        }
    }
}
