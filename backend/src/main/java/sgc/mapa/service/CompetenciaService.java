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

    public Competencia buscarPorCodigo(Long codCompetencia) {
        return competenciaRepo.findById(codCompetencia)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Competência", codCompetencia));
    }

    public List<Competencia> buscarPorCodMapa(Long codMapa) {
        return competenciaRepo.findByMapaCodigo(codMapa);
    }

    public void salvar(Competencia competencia) {
        competenciaRepo.save(competencia);
    }

    /**
     * Creates and persists competencia with associated activities
     */
    public void criarCompetenciaComAtividades(Mapa mapa, String descricao, List<Long> codigosAtividades) {
        Competencia competencia = new Competencia(descricao, mapa);
        prepararCompetenciasAtividades(codigosAtividades, competencia);
        competenciaRepo.save(competencia);

        atividadeRepo.saveAll(competencia.getAtividades());
    }

    /**
     * Updates competencia description and associated activities
     */
    public void atualizarCompetencia(Long codCompetencia, String descricao, List<Long> atividadesIds) {
        Competencia competencia = competenciaRepo.findById(codCompetencia)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Competência", codCompetencia));

        competencia.setDescricao(descricao);

        List<Atividade> atividadesAntigas = atividadeRepo.listarPorCompetencia(competencia);
        atividadesAntigas.forEach(atividade -> atividade.getCompetencias().remove(competencia));
        atividadeRepo.saveAll(atividadesAntigas);

        competencia.getAtividades().clear();
        prepararCompetenciasAtividades(atividadesIds, competencia);
        competenciaRepo.save(competencia);

        atividadeRepo.saveAll(competencia.getAtividades());
    }

    /**
     * Removes competence after dissociating from related activities
     */
    public void removerCompetencia(Long codCompetencia) {
        Competencia competencia = competenciaRepo.findById(codCompetencia)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Competência", codCompetencia));

        List<Atividade> atividadesAssociadas = atividadeRepo.listarPorCompetencia(competencia);
        atividadesAssociadas.forEach(atividade -> atividade.getCompetencias().remove(competencia));

        atividadeRepo.saveAll(atividadesAssociadas);
        competenciaRepo.delete(competencia);
    }

    /**
     * Prepares activities by associating with given competence
     */
    private void prepararCompetenciasAtividades(List<Long> codigosAtividades, Competencia competencia) {
        if (codigosAtividades.isEmpty()) return;

        List<Atividade> atividades = atividadeRepo.findAllById(codigosAtividades);
        competencia.setAtividades(new HashSet<>(atividades));

        atividades.forEach(atividade -> atividade.getCompetencias().add(competencia));
    }
}
