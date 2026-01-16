package sgc.subprocesso.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * DTO de requisição para criar um novo subprocesso.
 * 
 * <p>Usado exclusivamente como entrada de API para o endpoint de criação.
 * A validação Bean Validation é aplicada neste DTO.
 */
@Getter
@Builder
@AllArgsConstructor
public class CriarSubprocessoRequest {

    @NotNull(message = "O código do processo é obrigatório")
    private final Long codProcesso;

    @NotNull(message = "O código da unidade é obrigatório")
    private final Long codUnidade;

    private final Long codMapa;

    private final LocalDateTime dataLimiteEtapa1;
    private final LocalDateTime dataLimiteEtapa2;
}
