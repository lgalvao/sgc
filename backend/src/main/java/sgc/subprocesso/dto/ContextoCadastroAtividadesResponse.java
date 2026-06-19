package sgc.subprocesso.dto;

import org.jspecify.annotations.Nullable;
import sgc.mapa.dto.AtividadeDto;
import sgc.mapa.dto.MapaResumoDto;
import sgc.organizacao.dto.UnidadeResumoDto;

import java.util.List;

/**
 * Resposta enxuta para o cadastro de atividades.
 */
public record ContextoCadastroAtividadesResponse(
        @Nullable UnidadeResumoDto unidade,
        @Nullable SubprocessoDetalheResponse detalhes,
        @Nullable MapaResumoDto mapa,
        List<AtividadeDto> atividadesDisponiveis,
        String assinaturaCadastroReferencia
) {
}
