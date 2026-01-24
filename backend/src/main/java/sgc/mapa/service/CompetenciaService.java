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

    public List<Competencia> buscarPorCodMapaSemRelacionamentos(Long codMapa) {
        return competenciaRepo.findByMapaCodigoSemFetch(codMapa);
    }

    public java.util.Map<Long, java.util.Set<Long>> buscarIdsAssociacoesCompetenciaAtividade(Long codMapa) {
        List<Object[]> rows = competenciaRepo.findCompetenciaAndAtividadeIdsByMapaCodigo(codMapa);
        java.util.Map<Long, java.util.Set<Long>> result = new java.util.HashMap<>();
        for (Object[] row : rows) {
            Long compId = (Long) row[0];
            Long ativId = (Long) row[2];
            if (ativId != null) {
                result.computeIfAbsent(compId, k -> new java.util.HashSet<>()).add(ativId);
            }
        }
        return result;
    }

    public void salvar(Competencia competencia) {
        competenciaRepo.save(competencia);
    }

    public void salvarTodas(List<Competencia> competencias) {
        competenciaRepo.saveAll(competencias);
    }

    public List<Competencia> buscarPorCodigos(List<Long> codigos) {
        return competenciaRepo.findAllById(codigos);
    }

    /**
     * Creates and persists competencia with associated activities
     */
    public void criarCompetenciaComAtividades(Mapa mapa, String descricao, List<Long> codigosAtividades) {
        Competencia competencia = Competencia.builder()
                .descricao(descricao)
                .mapa(mapa)
                .build();
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
