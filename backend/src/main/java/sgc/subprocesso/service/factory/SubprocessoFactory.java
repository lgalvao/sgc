package sgc.subprocesso.service.factory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sgc.mapa.model.Mapa;
import sgc.mapa.model.MapaRepo;
import sgc.mapa.service.CopiaMapaService;
import sgc.organizacao.model.TipoUnidade;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.UnidadeMapa;
import sgc.processo.model.Processo;
import sgc.subprocesso.model.Movimentacao;
import sgc.subprocesso.model.MovimentacaoRepo;
import sgc.subprocesso.model.Subprocesso;

import java.util.List;

import static sgc.subprocesso.model.SituacaoSubprocesso.DIAGNOSTICO_AUTOAVALIACAO_EM_ANDAMENTO;
import static sgc.subprocesso.model.SituacaoSubprocesso.NAO_INICIADO;
import java.util.ArrayList;
import java.util.Collection;
import org.springframework.context.ApplicationEventPublisher;
import sgc.subprocesso.eventos.EventoTransicaoSubprocesso;
import sgc.subprocesso.eventos.TipoTransicao;
import sgc.subprocesso.model.SubprocessoRepo;

import sgc.subprocesso.dto.CriarSubprocessoRequest;

/**
 * Factory para criação de entidades Subprocesso.
 *
 * <p>Encapsula a lógica de construção de novos subprocessos com suas
 * dependências e configurações iniciais.
 *
 * <p><b>Nota arquitetural:</b> Mantido público temporariamente devido a uso em
 * {@link sgc.processo.service.ProcessoInicializador}. Considerar refatorar
 * para expor via Facade no futuro.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SubprocessoFactory {
    private final SubprocessoRepo subprocessoRepo;
    private final MapaRepo mapaRepo;
    private final MovimentacaoRepo movimentacaoRepo;
    private final CopiaMapaService servicoDeCopiaDeMapa;
    private final ApplicationEventPublisher eventPublisher;

    public Subprocesso criar(CriarSubprocessoRequest request) {
        Processo processo = Processo.builder()
                .codigo(request.codProcesso())
                .build();

        Unidade unidade = Unidade.builder()
                .codigo(request.codUnidade())
                .build();

        Subprocesso entity = Subprocesso.builder()
                .processo(processo)
                .unidade(unidade)
                .dataLimiteEtapa1(request.dataLimiteEtapa1())
                .dataLimiteEtapa2(request.dataLimiteEtapa2())
                .mapa(null)
                .build();

        Subprocesso subprocessoSalvo = subprocessoRepo.save(entity);

        Mapa mapa = Mapa.builder()
                .subprocesso(subprocessoSalvo)
                .build();
        Mapa mapaSalvo = mapaRepo.save(mapa);

        subprocessoSalvo.setMapa(mapaSalvo);
        return subprocessoRepo.save(subprocessoSalvo);
    }

    /**
     * Cria subprocessos para processo de mapeamento em lote.
     * Aplicável apenas a unidades OPERACIONAL ou INTEROPERACIONAL.
     */
    public void criarParaMapeamento(Processo processo, Collection<Unidade> unidades) {
        List<Unidade> unidadesElegiveis = unidades.stream()
                .filter(u -> TipoUnidade.OPERACIONAL == u.getTipo() || TipoUnidade.INTEROPERACIONAL == u.getTipo())
                .toList();

        if (unidadesElegiveis.isEmpty()) return;

        List<Subprocesso> subprocessos = unidadesElegiveis.stream()
                .map(unidade -> Subprocesso.builder()
                        .processo(processo)
                        .unidade(unidade)
                        .mapa(null)
                        .situacao(NAO_INICIADO)
                        .dataLimiteEtapa1(processo.getDataLimite())
                        .build())
                .map(Subprocesso.class::cast)
                .toList();

        List<Subprocesso> subprocessosSalvos = subprocessoRepo.saveAll(subprocessos);
        List<Mapa> mapas = subprocessosSalvos.stream()
                .<Mapa>map(sp -> Mapa.builder()
                        .subprocesso(sp)
                        .build())
                .toList();

        List<Mapa> mapasSalvos = mapaRepo.saveAll(mapas);
        for (int i = 0; i < subprocessosSalvos.size(); i++) {
            subprocessosSalvos.get(i).setMapa(mapasSalvos.get(i));
        }

        List<Movimentacao> movimentacoes = new ArrayList<>();
        for (Subprocesso sp : subprocessosSalvos) {
            movimentacoes.add(Movimentacao.builder()
                    .subprocesso(sp)
                    .unidadeDestino(sp.getUnidade())
                    .descricao("Processo iniciado")
                    .build());

            eventPublisher.publishEvent(EventoTransicaoSubprocesso.builder()
                    .subprocesso(sp)
                    .tipo(TipoTransicao.PROCESSO_INICIADO)
                    .unidadeDestino(sp.getUnidade())
                    .build());
        }
        movimentacaoRepo.saveAll(movimentacoes);
    }

    /**
     * Cria subprocesso para processo de revisão e copia o mapa vigente da unidade.
     */
    public void criarParaRevisao(Processo processo, Unidade unidade, UnidadeMapa unidadeMapa) {

        Long codMapaVigente = unidadeMapa.getMapaVigente().getCodigo();

        Subprocesso subprocesso = Subprocesso.builder()
                .processo(processo)
                .unidade(unidade)
                .mapa(null)
                .situacao(NAO_INICIADO)
                .dataLimiteEtapa1(processo.getDataLimite())
                .build();
        Subprocesso subprocessoSalvo = subprocessoRepo.save(subprocesso);

        Mapa mapaCopiado = servicoDeCopiaDeMapa.copiarMapaParaUnidade(codMapaVigente);

        mapaCopiado.setSubprocesso(subprocessoSalvo);
        Mapa mapaSalvo = mapaRepo.save(mapaCopiado);
        subprocessoSalvo.setMapa(mapaSalvo);

        movimentacaoRepo.save(Movimentacao.builder()
                .subprocesso(subprocessoSalvo)
                .unidadeDestino(unidade)
                .descricao("Processo de revisão iniciado")
                .build());

        eventPublisher.publishEvent(EventoTransicaoSubprocesso.builder()
                .subprocesso(subprocessoSalvo)
                .tipo(TipoTransicao.PROCESSO_INICIADO)
                .unidadeDestino(unidade)
                .build());

        log.info("Subprocesso para revisão criado para unidade {}", unidade.getSigla());
    }

    /**
     * Cria subprocesso para processo de diagnóstico.
     * Copia o mapa vigente e inicia com autoavaliação em andamento.
     */
    public void criarParaDiagnostico(Processo processo, Unidade unidade, UnidadeMapa unidadeMapa) {

        Long codMapaVigente = unidadeMapa.getMapaVigente().getCodigo();
        Subprocesso subprocesso = Subprocesso.builder()
                .processo(processo)
                .unidade(unidade)
                .mapa(null)
                .situacao(DIAGNOSTICO_AUTOAVALIACAO_EM_ANDAMENTO)
                .dataLimiteEtapa1(processo.getDataLimite())
                .build();

        Mapa mapaCopiado = servicoDeCopiaDeMapa.copiarMapaParaUnidade(codMapaVigente);
        Subprocesso subprocessoSalvo = subprocessoRepo.save(subprocesso);
        mapaCopiado.setSubprocesso(subprocessoSalvo);

        Mapa mapaSalvo = mapaRepo.save(mapaCopiado);
        subprocessoSalvo.setMapa(mapaSalvo);

        movimentacaoRepo.save(Movimentacao.builder()
                .subprocesso(subprocessoSalvo)
                .unidadeDestino(unidade)
                .descricao("Processo de diagnóstico iniciado")
                .build());

        eventPublisher.publishEvent(EventoTransicaoSubprocesso.builder()
                .subprocesso(subprocessoSalvo)
                .tipo(TipoTransicao.PROCESSO_INICIADO)
                .unidadeDestino(unidade)
                .build());

        log.info("Subprocesso {} para diagnóstico criado para unidade {}", subprocessoSalvo.getCodigo(), unidade.getSigla());
    }
}
