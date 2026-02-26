package sgc.subprocesso.dto;

import lombok.*;
import org.hibernate.validator.constraints.br.*;
import sgc.subprocesso.model.*;

import java.time.*;

@Builder
public record AnaliseHistoricoDto(
        TipoAnalise tipo,
        TipoAcaoAnalise acao,

        @TituloEleitoral
        String analistaUsuarioTitulo,

        String unidadeSigla,
        String unidadeNome,
        LocalDateTime dataHora,

        String motivo,
        String observacoes
) {
}
