package sgc.subprocesso.dto;

import sgc.mapa.dto.*;
import sgc.organizacao.model.*;

import java.util.*;

/**
 * Resposta agregada para o contexto de edição de um subprocesso.
 */
public record ContextoEdicaoResponse(
        Unidade unidade,
        SubprocessoResumoDto subprocesso,
        SubprocessoDetalheResponse detalhes,
        MapaCompletoDto mapa,
        List<AtividadeDto> atividadesDisponiveis
) {
}
