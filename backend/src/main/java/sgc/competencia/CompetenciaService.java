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
        if (atividadesIds != null && !atividadesIds.isEmpty()) {
            List<Atividade> atividades = atividadeRepo.findAllById(atividadesIds);
            for (Atividade atividade : atividades) {
                competenciaAtividadeRepo.save(new CompetenciaAtividade(new CompetenciaAtividade.Id(competencia.getCodigo(), atividade.getCodigo()), competencia, atividade));
            }
        }
    }

    public Competencia atualizarCompetencia(Long competenciaId, String descricao, List<Long> atividadesIds) {
        Competencia competencia = competenciaRepo.findById(competenciaId).orElseThrow(() -> new sgc.comum.erros.ErroDominioNaoEncontrado("Competência não encontrada"));
        competencia.setDescricao(descricao);

        List<CompetenciaAtividade> associacoesAntigas = competenciaAtividadeRepo.findByCompetenciaCodigo(competenciaId);
        competenciaAtividadeRepo.deleteAll(associacoesAntigas);

        if (atividadesIds != null && !atividadesIds.isEmpty()) {
            List<Atividade> atividades = atividadeRepo.findAllById(atividadesIds);
            for (Atividade atividade : atividades) {
                competenciaAtividadeRepo.save(new CompetenciaAtividade(new CompetenciaAtividade.Id(competencia.getCodigo(), atividade.getCodigo()), competencia, atividade));
            }
        }
        return competenciaRepo.save(competencia);
    }

    public void removerCompetencia(Long competenciaId) {
        List<CompetenciaAtividade> associacoes = competenciaAtividadeRepo.findByCompetenciaCodigo(competenciaId);
        competenciaAtividadeRepo.deleteAll(associacoes);
        competenciaRepo.deleteById(competenciaId);
    }
}
