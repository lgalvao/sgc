package sgc.subprocesso.dto;

import lombok.Builder;
import lombok.Getter;
import sgc.mapa.dto.MapaCompletoDto;
import sgc.sgrh.dto.UnidadeDto;

import java.util.List;

@Getter
@Builder
public class ContextoEdicaoDto {
    private final UnidadeDto unidade;
    private final SubprocessoDetalheDto subprocesso;
    private final MapaCompletoDto mapa;
    private final List<AtividadeVisualizacaoDto> atividadesDisponiveis;
}
