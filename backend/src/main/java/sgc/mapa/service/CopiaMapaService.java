package sgc.mapa.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.atividade.modelo.Atividade;
import sgc.atividade.modelo.AtividadeRepo;
import sgc.atividade.modelo.Conhecimento;
import sgc.atividade.modelo.ConhecimentoRepo;
import sgc.competencia.modelo.Competencia;
import sgc.competencia.modelo.CompetenciaAtividade;
import sgc.competencia.modelo.CompetenciaAtividadeRepo;
import sgc.competencia.modelo.CompetenciaRepo;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.mapa.modelo.Mapa;
import sgc.mapa.modelo.MapaRepo;
import sgc.unidade.modelo.Unidade;
import sgc.unidade.modelo.UnidadeRepo;

import java.util.*;

/**
 * Serviço responsável por copiar um mapa vigente para outra unidade.
 * A implementação deve clonar o mapa e suas atividades/conhecimentos mantendo integridade.
 */
@Service
@RequiredArgsConstructor
public class CopiaMapaService {
    private final MapaRepo repositorioMapa;
    private final AtividadeRepo atividadeRepo;
    private final ConhecimentoRepo conhecimentoRepo;
    private final UnidadeRepo repositorioUnidade;
    private final CompetenciaRepo competenciaRepo;
    private final CompetenciaAtividadeRepo competenciaAtividadeRepo;
    private final sgc.subprocesso.modelo.SubprocessoRepo subprocessoRepo;

    /**
     * Realiza uma cópia profunda de um mapa para uma nova unidade.
     * <p>
     * Este método cria uma nova instância de {@link Mapa} para a unidade de destino
     * e, em seguida, clona todas as suas {@link Competencia}s, {@link Atividade}s e
     * {@link Conhecimento}s associados, garantindo que as novas entidades estejam
     * corretamente vinculadas ao novo mapa.
     *
     * @param codMapaOrigem     O código do mapa a ser copiado.
     * @param codUnidadeDestino O código da unidade para a qual o mapa será copiado.
     * @return O novo {@link Mapa} criado e persistido.
     * @throws IllegalArgumentException se o mapa de origem ou a unidade de destino não forem encontrados.
     */
    @Transactional
    public Mapa copiarMapaParaUnidade(Long codMapaOrigem, Long codUnidadeDestino) {
        Mapa fonte = repositorioMapa.findById(codMapaOrigem)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Mapa", codMapaOrigem));

        Unidade unidadeDestino = repositorioUnidade.findById(codUnidadeDestino)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Unidade", codUnidadeDestino));

        Mapa novoMapa = new Mapa()
                .setDataHoraDisponibilizado(fonte.getDataHoraDisponibilizado())
                .setObservacoesDisponibilizacao(fonte.getObservacoesDisponibilizacao())
                .setSugestoesApresentadas(fonte.getSugestoesApresentadas())
                .setDataHoraHomologado(null)
                .setUnidade(unidadeDestino);

        Mapa mapaSalvo = repositorioMapa.save(novoMapa);
        subprocessoRepo.findByUnidadeCodigo(codUnidadeDestino).forEach(s -> s.setMapa(mapaSalvo));
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
        Map<Long, Competencia> mapa = new HashMap<>();
        if (competenciasFonte != null && !competenciasFonte.isEmpty()) {
            for (Competencia competenciaFonte : competenciasFonte) {
                Competencia novaCompetencia = new Competencia()
                        .setDescricao(competenciaFonte.getDescricao())
                        .setMapa(mapaSalvo);

                Competencia competenciaSalva = competenciaRepo.save(novaCompetencia);
                mapa.put(competenciaFonte.getCodigo(), competenciaSalva);
            }
        }

        List<CompetenciaAtividade> associacoesFonte = competenciaAtividadeRepo.findByCompetenciaMapaCodigo(fonte.getCodigo());
        if (associacoesFonte != null && !associacoesFonte.isEmpty()) {
            List<CompetenciaAtividade> novasAssociacoes = new ArrayList<>();
            for (CompetenciaAtividade associacaoFonte : associacoesFonte) {
                Competencia novaCompetencia = mapa.get(associacaoFonte.getCompetencia().getCodigo());
                Atividade novaAtividade = mapaDeAtividades.get(associacaoFonte.getAtividade().getCodigo());
                if (novaCompetencia != null && novaAtividade != null) {
                    novasAssociacoes.add(new CompetenciaAtividade(
                            new CompetenciaAtividade.Id(novaCompetencia.getCodigo(), novaAtividade.getCodigo()),
                            novaCompetencia,
                            novaAtividade));
                }
            }
            competenciaAtividadeRepo.saveAll(novasAssociacoes);
        }

        return mapaSalvo;
    }
}