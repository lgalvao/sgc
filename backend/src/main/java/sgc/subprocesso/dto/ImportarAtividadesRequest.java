package sgc.subprocesso.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import sgc.comum.Mensagens;

import java.util.List;

/**
 * DTO para a requisição de importação de atividades de outro subprocesso.
 */
@Builder
public record ImportarAtividadesRequest(
        @NotNull(message = Mensagens.CODIGO_SUBPROCESSO_ORIGEM_OBRIGATORIO)
        Long codSubprocessoOrigem,
        List<Long> codigosAtividades) {
}
