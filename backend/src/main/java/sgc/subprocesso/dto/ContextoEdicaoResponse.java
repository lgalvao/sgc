package sgc.subprocesso.dto;

import sgc.mapa.dto.*;
import sgc.mapa.model.*;
import sgc.organizacao.model.*;
import sgc.subprocesso.model.*;

import java.util.*;

/**
 * Resposta agregada para o contexto de edição de um subprocesso.
 */
public record ContextoEdicaoResponse(
        Unidade unidade,
        Subprocesso subprocesso,
        SubprocessoDetalheResponse detalhes,
        Mapa mapa,
        List<AtividadeDto> atividadesDisponiveis
) {
}
