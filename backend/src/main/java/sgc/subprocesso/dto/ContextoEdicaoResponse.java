package sgc.subprocesso.dto;

import org.jspecify.annotations.*;
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
        @Nullable SubprocessoDetalheResponse detalhes,
        @Nullable Mapa mapa,
        List<AtividadeDto> atividadesDisponiveis
) {
}
