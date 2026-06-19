package sgc.subprocesso.dto;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * DTO de requisição para atualizar um subprocesso existente.
 *
 * <p>
 * Usado exclusivamente como entrada de API para o endpoint de atualização.
 * Diferente da criação, não requer codProcesso pois o subprocesso já existe.
 */
@Builder
public record AtualizarSubprocessoRequest(
        Long codUnidade,
        Long codMapa,
        LocalDateTime dataLimiteEtapa1,
        LocalDateTime dataFimEtapa1,
        LocalDateTime dataLimiteEtapa2,
        LocalDateTime dataFimEtapa2) {
    public AtualizarSubprocessoCommand paraCommand() {
        return AtualizarSubprocessoCommand.builder()
                .vinculos(paraVinculosCommand())
                .prazos(paraPrazosCommand())
                .build();
    }

    public AtualizarVinculosSubprocessoCommand paraVinculosCommand() {
        return AtualizarVinculosSubprocessoCommand.builder()
                .codUnidade(codUnidade)
                .codMapa(codMapa)
                .build();
    }

    public sgc.subprocesso.dto.AtualizarPrazosSubprocessoCommand paraPrazosCommand() {
        return sgc.subprocesso.dto.AtualizarPrazosSubprocessoCommand.builder()
                .dataLimiteEtapa1(dataLimiteEtapa1 == null ? Optional.empty() : Optional.of(dataLimiteEtapa1))
                .dataFimEtapa1(Optional.ofNullable(dataFimEtapa1))
                .dataLimiteEtapa2(Optional.ofNullable(dataLimiteEtapa2))
                .dataFimEtapa2(Optional.ofNullable(dataFimEtapa2))
                .build();
    }
}
