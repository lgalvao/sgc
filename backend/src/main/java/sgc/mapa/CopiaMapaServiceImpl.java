package sgc.mapa;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.atividade.modelo.Atividade;
import sgc.atividade.modelo.AtividadeRepo;
import sgc.conhecimento.modelo.Conhecimento;
import sgc.conhecimento.modelo.ConhecimentoRepo;
import sgc.mapa.modelo.Mapa;
import sgc.mapa.modelo.MapaRepo;
import sgc.unidade.modelo.Unidade;
import sgc.unidade.modelo.UnidadeRepo;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementação do CopiaMapaService.
 * <p>
 * Realiza uma cópia profunda de um mapa, incluindo suas atividades e conhecimentos associados,
 * garantindo que as novas entidades sejam corretamente persistidas e relacionadas.
 */
@Service
@RequiredArgsConstructor
public class CopiaMapaServiceImpl implements CopiaMapaService {
    private final MapaRepo repositorioMapa;
    private final AtividadeRepo atividadeRepo;
    private final ConhecimentoRepo repositorioConhecimento;
    private final UnidadeRepo repositorioUnidade;

    @Override
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
        }

        for (Atividade atividadeFonte : atividadesFonte) {
            Long idAtividadeOriginal = atividadeFonte.getCodigo();
            List<Conhecimento> conhecimentosFonte = repositorioConhecimento.findByAtividadeCodigo(idAtividadeOriginal);

            if (conhecimentosFonte == null || conhecimentosFonte.isEmpty()) continue;

            Atividade novaAtividade = mapaDeAtividades.get(idAtividadeOriginal);
            if (novaAtividade == null) continue;

            for (Conhecimento conhecimentoFonte : conhecimentosFonte) {
                Conhecimento novoConhecimento = new Conhecimento();
                novoConhecimento.setAtividade(novaAtividade);
                novoConhecimento.setDescricao(conhecimentoFonte.getDescricao());
                repositorioConhecimento.save(novoConhecimento);
            }
        }
        return mapaSalvo;
    }
}