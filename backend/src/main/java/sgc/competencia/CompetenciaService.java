package sgc.competencia;

import sgc.competencia.modelo.Competencia;
import sgc.mapa.modelo.Mapa;
import java.util.List;

public interface CompetenciaService {
    Competencia adicionarCompetencia(Mapa mapa, String descricao, List<Long> atividadesIds);
    Competencia atualizarCompetencia(Long competenciaId, String descricao, List<Long> atividadesIds);
    void removerCompetencia(Long competenciaId);
}
