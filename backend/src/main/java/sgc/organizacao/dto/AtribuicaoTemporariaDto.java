package sgc.organizacao.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record AtribuicaoTemporariaDto(
        Long codigo,
        UnidadeDto unidade,
        UsuarioDto usuario,
        LocalDateTime dataInicio,
        LocalDateTime dataTermino,
        String justificativa) {
}
