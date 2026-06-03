package sgc.subprocesso.dto;

import org.jspecify.annotations.*;
import sgc.mapa.dto.*;
import sgc.organizacao.dto.*;

import java.util.*;

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
