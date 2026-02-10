package sgc.mapa.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.comum.repo.ComumRepo;
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
    private final ComumRepo repo;

    private final MapaRepo mapaRepo;
    private final AtividadeRepo atividadeRepo;
    private final CompetenciaRepo competenciaRepo;

    @Transactional
    public Mapa copiarMapaParaUnidade(Long codMapaOrigem) {
        Mapa fonte = mapaRepo
                .findById(codMapaOrigem)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Mapa", codMapaOrigem));

        Mapa novoMapa = criarNovoMapa(fonte);
        Mapa mapaSalvo = mapaRepo.save(novoMapa);

        Map<Long, Atividade> mapaAtividades = copiarAtividades(fonte.getCodigo(), mapaSalvo);
        copiarCompetencias(fonte.getCodigo(), mapaSalvo, mapaAtividades);

        return mapaSalvo;
    }

    /**
     * Importa atividades de um mapa de origem para um mapa de destino.
     * Apenas atividades que ainda não existem no destino serão importadas.
     */
    @Transactional
    public void importarAtividadesDeOutroMapa(Long mapaOrigemId, Long mapaDestinoId) {
        List<Atividade> atividadesOrigem = atividadeRepo.findWithConhecimentosByMapaCodigo(mapaOrigemId);
        Set<String> descricoesExistentes = obterDescricoesExistentes(mapaDestinoId);
        Mapa mapaDestino = repo.buscar(Mapa.class, mapaDestinoId);

        List<Atividade> atividadesParaSalvar = new ArrayList<>();
        for (Atividade atividadeOrigem : atividadesOrigem) {
            if (!descricoesExistentes.contains(atividadeOrigem.getDescricao())) {
                atividadesParaSalvar.add(prepararCopiaAtividade(atividadeOrigem, mapaDestino));
            }
        }

        if (!atividadesParaSalvar.isEmpty()) {
            atividadeRepo.saveAll(atividadesParaSalvar);
        }
    }

    private Mapa criarNovoMapa(Mapa fonte) {
        return Mapa.builder()
                .dataHoraDisponibilizado(fonte.getDataHoraDisponibilizado())
                .observacoesDisponibilizacao(fonte.getObservacoesDisponibilizacao())
                .dataHoraHomologado(null)
                .build();
    }

    private Map<Long, Atividade> copiarAtividades(Long codMapaFonte, Mapa mapaSalvo) {
        Map<Long, Atividade> mapaAtividades = new HashMap<>();
        List<Atividade> atividadesFonte = atividadeRepo.findWithConhecimentosByMapaCodigo(codMapaFonte);
        List<Atividade> novasAtividades = new ArrayList<>();
        Map<Atividade, Long> atividadeParaFonteId = new IdentityHashMap<>();

        for (Atividade atividadeFonte : atividadesFonte) {
            Atividade novaAtividade = prepararCopiaAtividade(atividadeFonte, mapaSalvo);
            novasAtividades.add(novaAtividade);
            atividadeParaFonteId.put(novaAtividade, atividadeFonte.getCodigo());
        }

        if (!novasAtividades.isEmpty()) {
            atividadeRepo.saveAll(novasAtividades);

            for (Atividade nova : novasAtividades) {
                Long fonteId = atividadeParaFonteId.get(nova);
                mapaAtividades.put(fonteId, nova);
            }
        }

        return mapaAtividades;
    }

    private Atividade prepararCopiaAtividade(Atividade atividadeFonte, Mapa mapaDestino) {
        Atividade novaAtividade = Atividade.builder()
                .descricao(atividadeFonte.getDescricao())
                .mapa(mapaDestino)
                .build();

        List<Conhecimento> conhecimentosFonte = atividadeFonte.getConhecimentos();
        if (!conhecimentosFonte.isEmpty()) {
            for (Conhecimento conhecimentoFonte : conhecimentosFonte) {
                Conhecimento novoConhecimento = Conhecimento.builder()
                        .atividade(novaAtividade)
                        .descricao(conhecimentoFonte.getDescricao())
                        .build();

                novaAtividade.getConhecimentos().add(novoConhecimento);
            }
        }

        return novaAtividade;
    }

    private void copiarCompetencias(Long codMapaFonte, Mapa mapaSalvo, Map<Long, Atividade> mapaAtividades) {
        List<Competencia> competenciasFonte = competenciaRepo.findByMapaCodigo(codMapaFonte);
        if (competenciasFonte.isEmpty()) return;

        List<Competencia> novasCompetencias = new ArrayList<>();

        for (Competencia competenciaFonte : competenciasFonte) {
            Set<Atividade> novasAtividadesAssociadas = new HashSet<>();
            for (Atividade atividadeFonteAssociada : competenciaFonte.getAtividades()) {
                Atividade novaAtividade = mapaAtividades.get(atividadeFonteAssociada.getCodigo());
                if (novaAtividade != null) {
                    novasAtividadesAssociadas.add(novaAtividade);
                }
            }

            Competencia novaCompetencia = Competencia.builder()
                    .descricao(competenciaFonte.getDescricao())
                    .mapa(mapaSalvo)
                    .atividades(novasAtividadesAssociadas)
                    .build();

            for (Atividade novaAtividade : novasAtividadesAssociadas) {
                novaAtividade.getCompetencias().add(novaCompetencia);
            }

            novasCompetencias.add(novaCompetencia);
        }

        competenciaRepo.saveAll(novasCompetencias);
    }

    private Set<String> obterDescricoesExistentes(Long mapaDestinoId) {
        return new HashSet<>(atividadeRepo.findByMapaCodigo(mapaDestinoId).stream()
                .map(Atividade::getDescricao)
                .toList());
    }
}
