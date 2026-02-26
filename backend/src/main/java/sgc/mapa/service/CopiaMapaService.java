package sgc.mapa.service;

import lombok.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import sgc.comum.model.*;
import sgc.mapa.model.*;

import java.util.*;

/**
 * Realiza cópias profundas de mapas de competências. Duplica toda a estrutura hierárquica de um mapa:
 * competências, atividades e conhecimentos, mantendo as associações entre eles.
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
        Mapa fonte = repo.buscar(Mapa.class, codMapaOrigem);

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
        List<Atividade> atividadesOrigem = atividadeRepo.findWithConhecimentosByMapa_Codigo(mapaOrigemId);
        Set<String> descExistentes = obterDescExistentes(mapaDestinoId);
        Mapa mapaDestino = repo.buscar(Mapa.class, mapaDestinoId);

        List<Atividade> atividadesParaSalvar = atividadesOrigem.stream()
                .filter(ativ -> !descExistentes.contains(ativ.getDescricao()))
                .map(ativ -> prepararCopiaAtividade(ativ, mapaDestino))
                .toList();

        if (!atividadesParaSalvar.isEmpty()) atividadeRepo.saveAll(atividadesParaSalvar);
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
        List<Atividade> atividadesFonte = atividadeRepo.findWithConhecimentosByMapa_Codigo(codMapaFonte);
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

        Set<Conhecimento> cs = atividadeFonte.getConhecimentos();
        if (!cs.isEmpty()) {
            cs.stream().map(c -> Conhecimento.builder()
                    .atividade(novaAtividade)
                    .descricao(c.getDescricao())
                    .build()).forEach(novoConhecimento -> novaAtividade.getConhecimentos().add(novoConhecimento));
        }

        return novaAtividade;
    }

    private void copiarCompetencias(Long codMapaFonte, Mapa mapaSalvo, Map<Long, Atividade> mapaAtividades) {
        List<Competencia> competenciasFonte = competenciaRepo.findByMapa_Codigo(codMapaFonte);
        if (competenciasFonte.isEmpty()) return;

        List<Competencia> novasCompetencias = new ArrayList<>();
        for (Competencia competenciaFonte : competenciasFonte) {
            Set<Atividade> novasAtividadesAssociadas = new HashSet<>();
            for (Atividade ativOrigemAssociada : competenciaFonte.getAtividades()) {
                Atividade novaAtividade = mapaAtividades.get(ativOrigemAssociada.getCodigo());
                if (novaAtividade != null) {
                    novasAtividadesAssociadas.add(novaAtividade);
                }
            }
            Competencia novaCompetencia = Competencia.builder()
                    .descricao(competenciaFonte.getDescricao())
                    .mapa(mapaSalvo)
                    .atividades(novasAtividadesAssociadas)
                    .build();

            novasAtividadesAssociadas.forEach(novaAtividade -> novaAtividade.getCompetencias().add(novaCompetencia));
            novasCompetencias.add(novaCompetencia);
        }

        competenciaRepo.saveAll(novasCompetencias);
    }

    private Set<String> obterDescExistentes(Long codMapaDestino) {
        return new HashSet<>(atividadeRepo.findByMapa_Codigo(codMapaDestino).stream()
                .map(Atividade::getDescricao)
                .toList());
    }
}
