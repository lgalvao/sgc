package sgc.subprocesso.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * DTO de requisição para atualizar um subprocesso existente.
 * 
 * <p>Usado exclusivamente como entrada de API para o endpoint de atualização.
 * Diferente da criação, não requer codProcesso pois o subprocesso já existe.
 */
@Getter
@Builder
@AllArgsConstructor
public class AtualizarSubprocessoRequest {
    private final Long codUnidade;
    private final Long codMapa;

    private final LocalDateTime dataLimiteEtapa1;
    private final LocalDateTime dataFimEtapa1;

    private final LocalDateTime dataLimiteEtapa2;
    private final LocalDateTime dataFimEtapa2;
}
