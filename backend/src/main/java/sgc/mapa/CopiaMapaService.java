package sgc.mapa;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.atividade.modelo.Atividade;
import sgc.atividade.modelo.AtividadeRepo;
import sgc.competencia.modelo.Competencia;
import sgc.competencia.modelo.CompetenciaRepo;
import sgc.conhecimento.modelo.Conhecimento;
import sgc.conhecimento.modelo.ConhecimentoRepo;
import sgc.mapa.modelo.Mapa;
import sgc.mapa.modelo.MapaRepo;
import sgc.unidade.modelo.Unidade;
import sgc.unidade.modelo.UnidadeRepo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Serviço responsável por copiar um mapa vigente para outra unidade.
 * A implementação deve clonar o mapa e suas atividades/conhecimentos mantendo integridade.
 */
@Service
@RequiredArgsConstructor
public class CopiaMapaService {
    private final MapaRepo repositorioMapa;
    private final AtividadeRepo atividadeRepo;
    private final ConhecimentoRepo repositorioConhecimento;
    private final UnidadeRepo repositorioUnidade;
    private final CompetenciaRepo competenciaRepo;

    /**
     * Copia o mapa identificado por sourceMapaId para a unidade targetUnidadeId.
     * Retorna o Mapa recém-criado (com novo id).
     *
     * @param idMapaFonte    id do mapa vigente a ser copiado
     * @param idUnidadeDestino id da unidade que receberá o mapa copiado
     * @return novo Mapa salvo
     * @throws IllegalArgumentException se o mapa fonte ou unidade alvo não existirem
     */
    @Transactional
    public Mapa copiarMapaParaUnidade(Long idMapaFonte, Long idUnidadeDestino) {
        Mapa fonte = repositorioMapa.findById(idMapaFonte)
                .orElseThrow(() -> new IllegalArgumentException("Mapa fonte não encontrado: %d".formatted(idMapaFonte)));

        Unidade unidadeDestino = repositorioUnidade.findById(idUnidadeDestino)
                .orElseThrow(() -> new IllegalArgumentException("Unidade de destino não encontrada: %d".formatted(idUnidadeDestino)));

        Mapa novoMapa = new Mapa();
        novoMapa.setDataHoraDisponibilizado(fonte.getDataHoraDisponibilizado());
        novoMapa.setObservacoesDisponibilizacao(fonte.getObservacoesDisponibilizacao());
        novoMapa.setSugestoesApresentadas(fonte.getSugestoesApresentadas());
        novoMapa.setDataHoraHomologado(null);
        novoMapa.setUnidade(unidadeDestino);

        Mapa mapaSalvo = repositorioMapa.save(novoMapa);
        Map<Long, Atividade> mapaDeAtividades = new HashMap<>();

        List<Atividade> atividadesFonte = atividadeRepo.findByMapaCodigo(fonte.getCodigo());
        if (atividadesFonte == null) {
            atividadesFonte = Collections.emptyList();
        }

        for (Atividade atividadeFonte : atividadesFonte) {
            Atividade novaAtividade = new Atividade();
            novaAtividade.setDescricao(atividadeFonte.getDescricao());
            novaAtividade.setMapa(mapaSalvo);
            Atividade atividadeSalva = atividadeRepo.save(novaAtividade);
            mapaDeAtividades.put(atividadeFonte.getCodigo(), atividadeSalva);

            List<Conhecimento> conhecimentosFonte = repositorioConhecimento.findByAtividadeCodigo(atividadeFonte.getCodigo());
            if (conhecimentosFonte != null && !conhecimentosFonte.isEmpty()) {
                List<Conhecimento> novosConhecimentos = new ArrayList<>();
                for (Conhecimento conhecimentoFonte : conhecimentosFonte) {
                    Conhecimento novoConhecimento = new Conhecimento();
                    novoConhecimento.setAtividade(atividadeSalva);
                    novoConhecimento.setDescricao(conhecimentoFonte.getDescricao());
                    novosConhecimentos.add(novoConhecimento);
                }
                repositorioConhecimento.saveAll(novosConhecimentos);
            }
        }

        List<Competencia> competenciasFonte = competenciaRepo.findByMapaCodigo(fonte.getCodigo());
        if (competenciasFonte != null && !competenciasFonte.isEmpty()) {
            List<Competencia> novasCompetencias = new ArrayList<>();
            for (Competencia competenciaFonte : competenciasFonte) {
                Competencia novaCompetencia = new Competencia();
                novaCompetencia.setDescricao(competenciaFonte.getDescricao());
                novaCompetencia.setMapa(mapaSalvo);
                novasCompetencias.add(novaCompetencia);
            }
            competenciaRepo.saveAll(novasCompetencias);
        }

        return mapaSalvo;
    }
}