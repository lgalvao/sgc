package sgc.mapa.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.AtividadeRepo;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.CompetenciaRepo;
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
        if (competencia.getAtividades() != null) {
            atividadeRepo.saveAll(competencia.getAtividades());
        }
    }

    public void atualizarCompetencia(Long codCompetencia, String descricao, List<Long> atividadesIds) {
        Competencia competencia = competenciaRepo
                .findById(codCompetencia)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Competência não encontrada"));

        competencia.setDescricao(descricao);

        Set<Atividade> atividadesAntigas = new HashSet<>(competencia.getAtividades());
        for (Atividade atividade : atividadesAntigas) {
            atividade.getCompetencias().remove(competencia);
        }
        atividadeRepo.saveAll(atividadesAntigas);

        competencia.getAtividades().clear();
        prepararCompetenciasAtividades(atividadesIds, competencia);
        competenciaRepo.save(competencia);

        if (competencia.getAtividades() != null) {
            atividadeRepo.saveAll(competencia.getAtividades());
        }
    }

    public void removerCompetencia(Long codCompetencia) {
        Competencia competencia = competenciaRepo
                .findById(codCompetencia)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Competência não encontrada"));

        for (Atividade atividade : competencia.getAtividades()) {
            atividade.getCompetencias().remove(competencia);
        }

        competenciaRepo.delete(competencia);
    }

    private void prepararCompetenciasAtividades(List<Long> codAtividades, Competencia competencia) {
        if (codAtividades == null || codAtividades.isEmpty()) return;

        List<Atividade> atividades = atividadeRepo.findAllById(codAtividades);
        competencia.setAtividades(new HashSet<>(atividades));
        for (Atividade atividade : atividades) {
            atividade.getCompetencias().add(competencia);
        }
    }
}
