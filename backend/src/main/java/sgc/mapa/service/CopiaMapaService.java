package sgc.mapa.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.mapa.model.*;

import java.util.*;

/**
 * Serviço especializado para realizar cópias profundas de mapas de competências.
 *
 * <p>Responsável por duplicar toda a estrutura hierárquica de um mapa:
 * competências, atividades e conhecimentos, mantendo as associações entre eles.
 *
 * <p>Essencial para iniciar novos ciclos de revisão baseados em mapas anteriores.
 */
@Service
@RequiredArgsConstructor
public class CopiaMapaService {
    private final MapaRepo repositorioMapa;
    private final AtividadeRepo atividadeRepo;
    private final ConhecimentoRepo conhecimentoRepo;
    private final CompetenciaRepo competenciaRepo;

    @Transactional
    public Mapa copiarMapaParaUnidade(Long codMapaOrigem) {
        Mapa fonte = repositorioMapa
                .findById(codMapaOrigem)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Mapa", codMapaOrigem));

        Mapa novoMapa = criarNovoMapa(fonte);
        Mapa mapaSalvo = repositorioMapa.save(novoMapa);

        Map<Long, Atividade> mapaAtividades = copiarAtividades(fonte.getCodigo(), mapaSalvo);
        copiarCompetencias(fonte.getCodigo(), mapaSalvo, mapaAtividades);

        return mapaSalvo;
    }

    /**
     * Importa atividades de um mapa de origem para um mapa de destino.
     * Apenas atividades que ainda não existem no destino serão importadas.
     *
     * @param mapaOrigemId  O código do mapa de origem.
     * @param mapaDestinoId O código do mapa de destino.
     * @throws ErroEntidadeNaoEncontrada se o mapa de destino não for encontrado.
     */
    @Transactional
    public void importarAtividadesDeOutroMapa(Long mapaOrigemId, Long mapaDestinoId) {
        List<Atividade> atividadesOrigem = atividadeRepo.findByMapaCodigoWithConhecimentos(mapaOrigemId);
        Set<String> descricoesExistentes = obterDescricoesExistentes(mapaDestinoId);

        Mapa mapaDestino = repositorioMapa.findById(mapaDestinoId)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Mapa", mapaDestinoId));

        for (Atividade atividadeOrigem : atividadesOrigem) {
            if (!descricoesExistentes.contains(atividadeOrigem.getDescricao())) {
                copiarAtividadeComConhecimentos(atividadeOrigem, mapaDestino);
            }
        }
    }

    // ===================================================================================
    // Métodos auxiliares de cópia
    // ===================================================================================

    private Mapa criarNovoMapa(Mapa fonte) {
        return new Mapa()
                .setDataHoraDisponibilizado(fonte.getDataHoraDisponibilizado())
                .setObservacoesDisponibilizacao(fonte.getObservacoesDisponibilizacao())
                .setDataHoraHomologado(null);
    }

    private Map<Long, Atividade> copiarAtividades(Long codMapaFonte, Mapa mapaSalvo) {
        Map<Long, Atividade> mapaAtividades = new HashMap<>();

        List<Atividade> atividadesFonte = atividadeRepo.findByMapaCodigoWithConhecimentos(codMapaFonte);
        if (atividadesFonte == null) atividadesFonte = List.of();

        for (Atividade atividadeFonte : atividadesFonte) {
            Atividade novaAtividade = copiarAtividadeComConhecimentos(atividadeFonte, mapaSalvo);
            mapaAtividades.put(atividadeFonte.getCodigo(), novaAtividade);
        }

        return mapaAtividades;
    }

    private Atividade copiarAtividadeComConhecimentos(Atividade atividadeFonte, Mapa mapaDestino) {
        Atividade novaAtividade = new Atividade();
        novaAtividade.setDescricao(atividadeFonte.getDescricao());
        novaAtividade.setMapa(mapaDestino);
        Atividade atividadeSalva = atividadeRepo.save(novaAtividade);

        copiarConhecimentos(atividadeFonte, atividadeSalva);

        return atividadeSalva;
    }

    private void copiarConhecimentos(Atividade atividadeFonte, Atividade atividadeSalva) {
        List<Conhecimento> conhecimentosFonte = atividadeFonte.getConhecimentos();
        if (conhecimentosFonte == null || conhecimentosFonte.isEmpty()) return;

        List<Conhecimento> novosConhecimentos = new ArrayList<>();
        for (Conhecimento conhecimentoFonte : conhecimentosFonte) {
            Conhecimento novoConhecimento = new Conhecimento()
                    .setAtividade(atividadeSalva)
                    .setDescricao(conhecimentoFonte.getDescricao());

            atividadeSalva.getConhecimentos().add(novoConhecimento);
            novosConhecimentos.add(novoConhecimento);
        }
        conhecimentoRepo.saveAll(novosConhecimentos);
    }

    private void copiarCompetencias(Long codMapaFonte, Mapa mapaSalvo, Map<Long, Atividade> mapaAtividades) {
        List<Competencia> competenciasFonte = competenciaRepo.findByMapaCodigo(codMapaFonte);
        if (competenciasFonte == null || competenciasFonte.isEmpty()) return;

        for (Competencia competenciaFonte : competenciasFonte) {
            Competencia novaCompetencia = new Competencia()
                    .setDescricao(competenciaFonte.getDescricao())
                    .setMapa(mapaSalvo);

            Set<Atividade> novasAtividadesAssociadas = new HashSet<>();
            for (Atividade atividadeFonteAssociada : competenciaFonte.getAtividades()) {
                Atividade novaAtividade = mapaAtividades.get(atividadeFonteAssociada.getCodigo());
                if (novaAtividade != null) {
                    novasAtividadesAssociadas.add(novaAtividade);
                    novaAtividade.getCompetencias().add(novaCompetencia);
                }
            }
            novaCompetencia.setAtividades(novasAtividadesAssociadas);

            competenciaRepo.save(novaCompetencia);
        }
    }

    private Set<String> obterDescricoesExistentes(Long mapaDestinoId) {
        return new HashSet<>(
                atividadeRepo.findByMapaCodigo(mapaDestinoId).stream()
                        .map(Atividade::getDescricao)
                        .toList()
        );
    }
}
