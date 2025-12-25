package sgc.mapa.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.AtividadeRepo;
import sgc.mapa.model.Conhecimento;
import sgc.mapa.model.ConhecimentoRepo;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.CompetenciaRepo;
import sgc.mapa.model.Mapa;
import sgc.mapa.model.MapaRepo;

import java.util.*;

@Service
@RequiredArgsConstructor
public class CopiaMapaService {
    private final MapaRepo repositorioMapa;
    private final AtividadeRepo atividadeRepo;
    private final ConhecimentoRepo conhecimentoRepo;
    private final CompetenciaRepo competenciaRepo;

    @Transactional
    public Mapa copiarMapaParaUnidade(Long codMapaOrigem, Long codUnidadeDestino) {
        Mapa fonte = repositorioMapa
                .findById(codMapaOrigem)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Mapa", codMapaOrigem));

        Mapa novoMapa = new Mapa()
                .setDataHoraDisponibilizado(fonte.getDataHoraDisponibilizado())
                .setObservacoesDisponibilizacao(fonte.getObservacoesDisponibilizacao())
                .setDataHoraHomologado(null);

        Mapa mapaSalvo = repositorioMapa.save(novoMapa);

        Map<Long, Atividade> mapaDeAtividades = new HashMap<>();

        // Optimization: Use findByMapaCodigoWithConhecimentos to fetch activities and their knowledges
        // in a single query (LEFT JOIN FETCH), avoiding the N+1 problem inside the loop.
        List<Atividade> atividadesFonte = atividadeRepo.findByMapaCodigoWithConhecimentos(fonte.getCodigo());
        if (atividadesFonte == null) {
            atividadesFonte = Collections.emptyList();
        }

        for (Atividade atividadeFonte : atividadesFonte) {
            Atividade novaAtividade = new Atividade();
            novaAtividade.setDescricao(atividadeFonte.getDescricao());
            novaAtividade.setMapa(mapaSalvo);
            Atividade atividadeSalva = atividadeRepo.save(novaAtividade);
            mapaDeAtividades.put(atividadeFonte.getCodigo(), atividadeSalva);

            // Accessing the collection directly as it was pre-fetched
            List<Conhecimento> conhecimentosFonte = atividadeFonte.getConhecimentos();
            if (conhecimentosFonte != null && !conhecimentosFonte.isEmpty()) {
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
        }

        List<Competencia> competenciasFonte = competenciaRepo.findByMapaCodigo(fonte.getCodigo());
        if (competenciasFonte != null && !competenciasFonte.isEmpty()) {
            for (Competencia competenciaFonte : competenciasFonte) {
                Competencia novaCompetencia = new Competencia()
                        .setDescricao(competenciaFonte.getDescricao())
                        .setMapa(mapaSalvo);

                Set<Atividade> novasAtividadesAssociadas = new HashSet<>();
                for (Atividade atividadeFonteAssociada : competenciaFonte.getAtividades()) {
                    Atividade novaAtividade = mapaDeAtividades.get(atividadeFonteAssociada.getCodigo());
                    if (novaAtividade != null) {
                        novasAtividadesAssociadas.add(novaAtividade);
                        novaAtividade.getCompetencias().add(novaCompetencia);
                    }
                }
                novaCompetencia.setAtividades(novasAtividadesAssociadas);

                competenciaRepo.save(novaCompetencia);
            }
        }

        return mapaSalvo;
    }
}
