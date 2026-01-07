package sgc.mapa.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.mapa.model.*;

import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CompetenciaService {
    private final CompetenciaRepo competenciaRepo;
    private final AtividadeRepo atividadeRepo;

    public List<Competencia> buscarPorMapa(Long mapaId) {
        return competenciaRepo.findByMapaCodigo(mapaId);
    }

    public Competencia buscarPorId(Long id) {
        return competenciaRepo.findById(id)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Competência", id));
    }

    public void salvar(Competencia competencia) {
        competenciaRepo.save(competencia);
    }

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

        // ⚡ Bolt: Otimização N+1.
        // Busca atividades com suas competências já carregadas para evitar queries no loop de remoção.
        List<Atividade> atividadesAntigas = atividadeRepo.listarPorCompetencia(competencia);
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

        // ⚡ Bolt: Otimização N+1.
        // Busca atividades com suas competências já carregadas.
        List<Atividade> atividadesAssociadas = atividadeRepo.listarPorCompetencia(competencia);

        for (Atividade atividade : atividadesAssociadas) {
            atividade.getCompetencias().remove(competencia);
        }
        // É importante salvar as atividades se a cascata não for automática, mas JPA gerencia a coleção.
        // Explicitamente salvando para garantir sincronia.
        atividadeRepo.saveAll(atividadesAssociadas);

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
