package sgc.mapa.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.atividade.model.Atividade;
import sgc.atividade.model.AtividadeRepo;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.CompetenciaRepo;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.mapa.model.Mapa;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class CompetenciaService {
    private final CompetenciaRepo competenciaRepo;
    private final AtividadeRepo atividadeRepo;

    public void adicionarCompetencia(Mapa mapa, String descricao, List<Long> atividadesIds) {
        Competencia competencia = new Competencia(descricao, mapa);
        prepararCompetenciasAtividades(atividadesIds, competencia);
        competenciaRepo.save(competencia);
    }

    public Competencia atualizarCompetencia(Long codCompetencia, String descricao, List<Long> atividadesIds) {
        Competencia competencia = competenciaRepo.findById(codCompetencia).orElseThrow(
                () -> new ErroEntidadeNaoEncontrada("Competência não encontrada"));

        competencia.setDescricao(descricao);
        competencia.getAtividades().clear();
        prepararCompetenciasAtividades(atividadesIds, competencia);
        return competenciaRepo.save(competencia);
    }

    public void removerCompetencia(Long codCompetencia) {
        competenciaRepo.deleteById(codCompetencia);
    }

    private void prepararCompetenciasAtividades(List<Long> codAtividades, Competencia competencia) {
        if (codAtividades == null || codAtividades.isEmpty()) return;

        List<Atividade> atividades = atividadeRepo.findAllById(codAtividades);
        competencia.setAtividades(new HashSet<>(atividades));
    }
}
