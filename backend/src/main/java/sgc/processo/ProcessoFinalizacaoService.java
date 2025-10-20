package sgc.processo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.erros.ErroDominioNaoEncontrado;
import sgc.mapa.modelo.Mapa;
import sgc.mapa.modelo.UnidadeMapa;
import sgc.mapa.modelo.UnidadeMapaRepo;
import sgc.processo.eventos.ProcessoFinalizadoEvento;
import sgc.processo.modelo.ErroProcesso;
import sgc.processo.modelo.Processo;
import sgc.processo.modelo.ProcessoRepo;
import sgc.processo.modelo.UnidadeProcessoRepo;
import sgc.subprocesso.SituacaoSubprocesso;
import sgc.subprocesso.modelo.Subprocesso;
import sgc.subprocesso.modelo.SubprocessoRepo;
import sgc.unidade.modelo.Unidade;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProcessoFinalizacaoService {
    private final ProcessoRepo processoRepo;
    private final SubprocessoRepo subprocessoRepo;
    private final UnidadeMapaRepo unidadeMapaRepo;
    private final UnidadeProcessoRepo unidadeProcessoRepo;
    private final ApplicationEventPublisher publicadorDeEventos;
    private final ProcessoNotificacaoService processoNotificacaoService;

    /**
     * Finaliza um processo, realizando todas as validações e atualizações necessárias.
     * <p>
     * O processo de finalização inclui:
     * <ul>
     *     <li>Verificar se o processo está na situação 'EM_ANDAMENTO'.</li>
     *     <li>Validar se todos os seus subprocessos estão com a situação 'MAPA_HOMOLOGADO'.</li>
     *     <li>Tornar os mapas de cada subprocesso como o mapa vigente para a respectiva unidade.</li>
     *     <li>Atualizar a situação e a data de finalização do processo.</li>
     *     <li>Enviar notificações de finalização.</li>
     *     <li>Publicar um evento {@link ProcessoFinalizadoEvento}.</li>
     * </ul>
     *
     * @param id O ID do processo a ser finalizado.
     * @throws ErroDominioNaoEncontrado se o processo não for encontrado.
     * @throws ErroProcesso se o processo não puder ser finalizado devido à sua
     *                      situação atual ou à de seus subprocessos.
     */
    @Transactional
    public void finalizar(Long id) {
        log.info("Iniciando finalização do processo: código={}", id);

        Processo processo = processoRepo.findById(id)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Processo", id));

        validarFinalizacaoProcesso(processo);
        tornarMapasVigentes(processo);

        processo.setSituacao(SituacaoProcesso.FINALIZADO);
        processo.setDataFinalizacao(LocalDateTime.now());

        processoRepo.save(processo);
        processoNotificacaoService.enviarNotificacoesDeFinalizacao(processo, unidadeProcessoRepo.findByProcessoCodigo(processo.getCodigo()));
        publicadorDeEventos.publishEvent(new ProcessoFinalizadoEvento(this, processo.getCodigo()));

        log.info("Processo finalizado com sucesso: código={}", id);
    }

    private void validarFinalizacaoProcesso(Processo processo) {
        if (processo.getSituacao() != SituacaoProcesso.EM_ANDAMENTO) {
            throw new ErroProcesso("Apenas processos 'EM ANDAMENTO' podem ser finalizados.");
        }
        validarTodosSubprocessosHomologados(processo);
    }

    private void validarTodosSubprocessosHomologados(Processo processo) {
        log.debug("Validando homologação de subprocessos do processo {}", processo.getCodigo());
        List<Subprocesso> subprocessos = subprocessoRepo.findByProcessoCodigoWithUnidade(processo.getCodigo());

        List<String> pendentes = subprocessos.stream()
            .filter(sp -> sp.getSituacao() != SituacaoSubprocesso.MAPA_HOMOLOGADO)
            .map(sp -> String.format("%s (Situação: %s)",
                sp.getUnidade() != null ? sp.getUnidade().getSigla() : "Subprocesso " + sp.getCodigo(),
                sp.getSituacao()))
            .toList();

        if (!pendentes.isEmpty()) {
            String mensagem = String.format(
                "Não é possível encerrar o processo. Unidades pendentes de homologação:%n- %s",
                String.join("%n- ", pendentes)
            );
            log.warn("Validação de finalização falhou: {} subprocessos não homologados.", pendentes.size());
            throw new ErroProcesso(mensagem);
        }
        log.info("Validação OK: {} subprocessos homologados.", subprocessos.size());
    }

    private void tornarMapasVigentes(Processo processo) {
        log.info("Tornando mapas vigentes para o processo {}", processo.getCodigo());
        List<Subprocesso> subprocessos = subprocessoRepo.findByProcessoCodigoWithUnidade(processo.getCodigo());

        for (Subprocesso subprocesso : subprocessos) {
            Long codigoUnidade = Optional.ofNullable(subprocesso.getUnidade()).map(Unidade::getCodigo)
                .orElseThrow(() -> new ErroProcesso("Subprocesso " + subprocesso.getCodigo() + " sem unidade associada."));

            Mapa mapaDoSubprocesso = Optional.ofNullable(subprocesso.getMapa())
                .orElseThrow(() -> new ErroProcesso("Subprocesso " + subprocesso.getCodigo() + " sem mapa associado."));

            UnidadeMapa unidadeMapa = unidadeMapaRepo.findByUnidadeCodigo(codigoUnidade)
                .orElse(new UnidadeMapa(codigoUnidade));

            unidadeMapa.setMapaVigenteCodigo(mapaDoSubprocesso.getCodigo());
            unidadeMapa.setDataVigencia(LocalDateTime.now());
            unidadeMapaRepo.save(unidadeMapa);

            log.debug("Mapa vigente para unidade {} definido como mapa {}", codigoUnidade, mapaDoSubprocesso.getCodigo());
        }

        log.info("Mapas de {} subprocessos foram definidos como vigentes.", subprocessos.size());
    }
}