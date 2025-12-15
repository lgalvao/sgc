package sgc.mapa.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.atividade.model.Atividade;
import sgc.atividade.model.AtividadeRepo;
import sgc.atividade.model.Conhecimento;
import sgc.atividade.model.ConhecimentoRepo;
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

            List<Conhecimento> conhecimentosFonte = conhecimentoRepo.findByAtividadeCodigo(atividadeFonte.getCodigo());
            if (conhecimentosFonte != null && !conhecimentosFonte.isEmpty()) {
                List<Conhecimento> novosConhecimentos = new ArrayList<>();
                for (Conhecimento conhecimentoFonte : conhecimentosFonte) {
                    Conhecimento novoConhecimento = new Conhecimento()
                            .setAtividade(atividadeSalva)
                            .setDescricao(conhecimentoFonte.getDescricao());

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
                    novasAtividadesAssociadas.add(mapaDeAtividades.get(atividadeFonteAssociada.getCodigo()));
                }
                novaCompetencia.setAtividades(novasAtividadesAssociadas);

                competenciaRepo.save(novaCompetencia);
            }
        }

        return mapaSalvo;
    }
}
