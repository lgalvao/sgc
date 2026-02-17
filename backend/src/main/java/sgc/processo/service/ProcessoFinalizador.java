package sgc.processo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.repo.ComumRepo;
import sgc.mapa.model.Mapa;
import sgc.organizacao.UnidadeFacade;
import sgc.organizacao.model.Unidade;
import sgc.processo.erros.ErroProcesso;
import sgc.processo.model.Processo;
import sgc.processo.model.ProcessoRepo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.query.ConsultasSubprocessoService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Serviço responsável pela finalização de processos.
 *
 * <p>Coordena todas as etapas necessárias para finalizar um processo,
 * incluindo validações, publicação de mapas vigentes e eventos.</p>
 *
 * <p><b>Refatoração v3.0:</b> Removido uso de @Lazy e dependência circular.
 * Agora utiliza {@link ConsultasSubprocessoService} para queries de leitura,
 * eliminando acoplamento bidirecional com SubprocessoFacade.</p>
 *
 * @since 3.0.0 - Removido @Lazy, introduzido Query Service Pattern
 */
@Service
@Slf4j
@RequiredArgsConstructor
class ProcessoFinalizador {

    private final ProcessoRepo processoRepo;
    private final ComumRepo repo;
    private final UnidadeFacade unidadeService;
    private final ConsultasSubprocessoService queryService;
    private final ProcessoValidador processoValidador;
    private final ProcessoNotificacaoService notificacaoService;


    /**
     * Finaliza um processo, validando e tornando os mapas vigentes.
     *
     * @param codigo código do processo a finalizar
     * @throws ErroProcesso              se o processo não puder ser finalizado
     */
    @Transactional
    public void finalizar(Long codigo) {
        Processo processo = repo.buscar(Processo.class, codigo);

        processoValidador.validarFinalizacaoProcesso(processo);
        
        if (processo.getTipo() != TipoProcesso.DIAGNOSTICO) {
            tornarMapasVigentes(processo);
        }

        processo.setSituacao(SituacaoProcesso.FINALIZADO);
        processo.setDataFinalizacao(LocalDateTime.now());

        processoRepo.save(processo);

        // Notificar diretamente (sem evento assíncrono)
        notificacaoService.notificarFinalizacaoProcesso(processo.getCodigo());

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
        List<Subprocesso> subprocessos = queryService.listarEntidadesPorProcesso(processo.getCodigo());

        for (Subprocesso subprocesso : subprocessos) {
            Unidade unidade = Optional.ofNullable(subprocesso.getUnidade())
                    .orElseThrow(() -> new ErroProcesso(
                            "Subprocesso %d sem unidade associada.".formatted(subprocesso.getCodigo())));

            Mapa mapaDoSubprocesso = Optional.ofNullable(subprocesso.getMapa())
                    .orElseThrow(() -> new ErroProcesso(
                            "Subprocesso %d sem mapa associado.".formatted(subprocesso.getCodigo())));

            unidadeService.definirMapaVigente(unidade.getCodigo(), mapaDoSubprocesso);
        }
        log.info("Mapa(s) de {} subprocesso(s) definidos como vigentes.", subprocessos.size());
    }
}
