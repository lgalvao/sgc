package sgc.processo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.mapa.model.Mapa;
import sgc.organizacao.UnidadeFacade;
import sgc.organizacao.model.Unidade;
import sgc.processo.erros.ErroProcesso;
import sgc.processo.eventos.EventoProcessoFinalizado;
import sgc.processo.model.Processo;
import sgc.processo.model.ProcessoRepo;
import sgc.processo.model.SituacaoProcesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.SubprocessoFacade;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Serviço responsável pela finalização de processos.
 * 
 * <p>Coordena todas as etapas necessárias para finalizar um processo,
 * incluindo validações, publicação de mapas vigentes e eventos.</p>
 * 
 * <p><b>Nota sobre Injeção de Dependências:</b>
 * SubprocessoFacade é injetado com @Lazy para quebrar dependência circular:
 * ProcessoFacade → ProcessoFinalizador → SubprocessoFacade → ... → ProcessoFacade
 */
@Service
@Slf4j
class ProcessoFinalizador {
    
    private final ProcessoRepo processoRepo;
    private final UnidadeFacade unidadeService;
    private final SubprocessoFacade subprocessoFacade;
    private final ProcessoValidador processoValidador;
    private final ApplicationEventPublisher publicadorEventos;

    /**
     * Constructor com @Lazy para quebrar dependência circular.
     */
    public ProcessoFinalizador(
            ProcessoRepo processoRepo,
            UnidadeFacade unidadeService,
            @Lazy SubprocessoFacade subprocessoFacade,
            ProcessoValidador processoValidador,
            ApplicationEventPublisher publicadorEventos) {
        this.processoRepo = processoRepo;
        this.unidadeService = unidadeService;
        this.subprocessoFacade = subprocessoFacade;
        this.processoValidador = processoValidador;
        this.publicadorEventos = publicadorEventos;
    }

    /**
     * Finaliza um processo, validando e tornando os mapas vigentes.
     * 
     * @param codigo código do processo a finalizar
     * @throws ErroEntidadeNaoEncontrada se o processo não for encontrado
     * @throws ErroProcesso se o processo não puder ser finalizado
     */
    @Transactional
    public void finalizar(Long codigo) {
        Processo processo = processoRepo.findById(codigo)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Processo", codigo));

        processoValidador.validarFinalizacaoProcesso(processo);
        tornarMapasVigentes(processo);

        processo.setSituacao(SituacaoProcesso.FINALIZADO);
        processo.setDataFinalizacao(LocalDateTime.now());

        processoRepo.save(processo);
        publicadorEventos.publishEvent(
                EventoProcessoFinalizado.builder()
                        .codProcesso(processo.getCodigo())
                        .dataHoraFinalizacao(LocalDateTime.now())
                        .build());

        log.info("Processo {} finalizado", codigo);
    }

    /**
     * Torna os mapas de todos os subprocessos do processo como vigentes.
     * 
     * @param processo processo cujos mapas serão tornados vigentes
     * @throws ErroProcesso se algum subprocesso não tiver unidade ou mapa associado
     */
    private void tornarMapasVigentes(Processo processo) {
        log.info("Mapa vigente definido para o processo {}", processo.getCodigo());
        List<Subprocesso> subprocessos = subprocessoFacade.listarEntidadesPorProcesso(processo.getCodigo());

        for (Subprocesso subprocesso : subprocessos) {
            Unidade unidade = Optional.of(subprocesso.getUnidade())
                    .orElseThrow(() -> new ErroProcesso(
                            "Subprocesso %d sem unidade associada.".formatted(subprocesso.getCodigo())));

            Mapa mapaDoSubprocesso = Optional.of(subprocesso.getMapa())
                    .orElseThrow(() -> new ErroProcesso(
                            "Subprocesso %d sem mapa associado.".formatted(subprocesso.getCodigo())));

            unidadeService.definirMapaVigente(unidade.getCodigo(), mapaDoSubprocesso);
        }
        log.info("Mapa(s) de {} subprocesso(s) definidos como vigentes.", subprocessos.size());
    }
}
