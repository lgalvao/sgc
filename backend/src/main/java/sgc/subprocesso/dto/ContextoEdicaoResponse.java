package sgc.subprocesso.dto;

import org.jspecify.annotations.*;
import sgc.mapa.dto.*;
import sgc.organizacao.model.*;
import sgc.subprocesso.model.*;

import java.util.*;

/**
 * Resposta agregada para o contexto de edição de um subprocesso.
 */
public record ContextoEdicaoResponse(
        Unidade unidade,
        SubprocessoResumoDto subprocesso,
        @Nullable SubprocessoDetalheResponse detalhes,
        @Nullable MapaCompletoDto mapa,
        List<AtividadeDto> atividadesDisponiveis
) {
}
