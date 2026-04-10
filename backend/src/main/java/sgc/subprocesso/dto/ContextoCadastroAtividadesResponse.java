package sgc.subprocesso.dto;

import sgc.mapa.dto.*;
import sgc.organizacao.model.*;

import java.util.*;

/**
 * Resposta enxuta para o cadastro de atividades.
 */
public record ContextoCadastroAtividadesResponse(
        Unidade unidade,
        SubprocessoDetalheResponse detalhes,
        MapaResumoDto mapa,
        List<AtividadeDto> atividadesDisponiveis
) {
}
