package sgc.subprocesso.dto;

import lombok.Builder;
import lombok.Getter;
import sgc.atividade.dto.AtividadeDto;
import sgc.mapa.dto.MapaCompletoDto;
import sgc.sgrh.dto.UnidadeDto;

import java.util.List;

/**
 * DTO que agrega todas as informações necessárias para carregar o contexto de edição de mapa (CDU-14).
 * Evita múltiplas requisições do frontend ("API Chaining").
 */
@Getter
@Builder
public class ContextoEdicaoDto {
    private final UnidadeDto unidade;
    private final SubprocessoDetalheDto subprocesso;
    private final MapaCompletoDto mapa;
    private final List<AtividadeDto> atividadesDisponiveis;
}
