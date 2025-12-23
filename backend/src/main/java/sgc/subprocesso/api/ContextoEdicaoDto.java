package sgc.subprocesso.api;

import lombok.Builder;
import lombok.Getter;
import sgc.mapa.api.MapaCompletoDto;
import sgc.sgrh.api.UnidadeDto;

import java.util.List;

@Getter
@Builder
public class ContextoEdicaoDto {
    private final UnidadeDto unidade;
    private final SubprocessoDetalheDto subprocesso;
    private final MapaCompletoDto mapa;
    private final List<AtividadeVisualizacaoDto> atividadesDisponiveis;
}
