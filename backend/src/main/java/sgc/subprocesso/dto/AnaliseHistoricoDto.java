package sgc.subprocesso.dto;

import lombok.Builder;
import org.hibernate.validator.constraints.br.TituloEleitoral;

import java.time.LocalDateTime;

@Builder
public record AnaliseHistoricoDto(
        String tipo,
        String acao,
        String acaoDescricao,

        @TituloEleitoral
        String analistaUsuarioTitulo,
        String usuarioNome,

        String unidadeSigla,
        String unidadeNome,
        LocalDateTime dataHora,

        String motivo,
        String observacoes
) {
}
