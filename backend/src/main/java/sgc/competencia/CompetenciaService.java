package sgc.competencia;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.atividade.modelo.Atividade;
import sgc.atividade.modelo.AtividadeRepo;
import sgc.competencia.modelo.Competencia;
import sgc.competencia.modelo.CompetenciaAtividade;
import sgc.competencia.modelo.CompetenciaAtividadeRepo;
import sgc.competencia.modelo.CompetenciaRepo;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.mapa.modelo.Mapa;

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
