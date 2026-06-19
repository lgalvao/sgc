package sgc.mapa.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import org.jspecify.annotations.Nullable;
import sgc.comum.Mensagens;
import sgc.seguranca.sanitizacao.SanitizarHtmlFormatado;

import java.util.List;

/**
 * DTO de requisição para salvar mapa. CDU-09 item 1 a 4.
 */
@Builder
public record SalvarMapaRequest(
        @Nullable @Size(max = 500, message = Mensagens.OBSERVACOES_MAX_500) @SanitizarHtmlFormatado String observacoes,
        @Valid List<CompetenciaRequest> competencias) {

    @Builder
    public record CompetenciaRequest(
            Long codigo,

            @NotBlank(message = Mensagens.DESCRICAO_COMPETENCIA_OBRIGATORIA) String descricao,
            List<Long> atividadesCodigos
    ) {
    }
}
