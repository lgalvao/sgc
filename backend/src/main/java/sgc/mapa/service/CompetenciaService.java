package sgc.mapa.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.atividade.model.Atividade;
import sgc.atividade.model.AtividadeRepo;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.CompetenciaAtividade;
import sgc.mapa.model.CompetenciaAtividadeRepo;
import sgc.mapa.model.CompetenciaRepo;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.mapa.model.Mapa;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CompetenciaService {
    private final CompetenciaRepo competenciaRepo;
    private final AtividadeRepo atividadeRepo;
    private final CompetenciaAtividadeRepo competenciaAtividadeRepo;

    public void adicionarCompetencia(Mapa mapa, String descricao, List<Long> atividadesIds) {
        Competencia competencia = new Competencia(descricao, mapa);
        competenciaRepo.save(competencia);
        prepararCompetenciasAtividades(atividadesIds, competencia);
    }

    public Competencia atualizarCompetencia(Long codCompetencia, String descricao, List<Long> atividadesIds) {
        Competencia competencia = competenciaRepo.findById(codCompetencia).orElseThrow(
                () -> new ErroEntidadeNaoEncontrada("Competência não encontrada"));

        competencia.setDescricao(descricao);

        List<CompetenciaAtividade> associacoesAntigas = competenciaAtividadeRepo.findByCompetenciaCodigo(codCompetencia);
        competenciaAtividadeRepo.deleteAll(associacoesAntigas);

        prepararCompetenciasAtividades(atividadesIds, competencia);
        return competenciaRepo.save(competencia);
    }

    public void removerCompetencia(Long codCompetencia) {
        List<CompetenciaAtividade> associacoes = competenciaAtividadeRepo.findByCompetenciaCodigo(codCompetencia);
        competenciaAtividadeRepo.deleteAll(associacoes);
        competenciaRepo.deleteById(codCompetencia);
    }

    private void prepararCompetenciasAtividades(List<Long> codAtividades, Competencia competencia) {
        if (codAtividades == null || codAtividades.isEmpty()) return;

        List<Atividade> atividades = atividadeRepo.findAllById(codAtividades);
        for (Atividade atividade : atividades) {
            CompetenciaAtividade ca = new CompetenciaAtividade(
                    new CompetenciaAtividade.Id(competencia.getCodigo(), atividade.getCodigo()), competencia, atividade
            );
            competenciaAtividadeRepo.save(ca);
        }
    }
}
