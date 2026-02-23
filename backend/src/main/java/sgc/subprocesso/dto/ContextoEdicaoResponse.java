package sgc.subprocesso.dto;

import sgc.mapa.dto.AtividadeDto;
import sgc.mapa.model.Mapa;
import sgc.organizacao.model.Unidade;
import sgc.subprocesso.model.Subprocesso;

import java.util.List;

/**
 * Resposta agregada para o contexto de edição de um subprocesso.
 */
public record ContextoEdicaoResponse(
    Unidade unidade,
    Subprocesso subprocesso,
    SubprocessoDetalheResponse detalhes,
    Mapa mapa,
    List<AtividadeDto> atividadesDisponiveis
) {}
