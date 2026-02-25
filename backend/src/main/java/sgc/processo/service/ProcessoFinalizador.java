package sgc.processo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.model.ComumRepo;
import sgc.mapa.model.Mapa;
import sgc.organizacao.OrganizacaoFacade;
import sgc.organizacao.model.Unidade;
import sgc.processo.model.Processo;
import sgc.processo.model.ProcessoRepo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.ConsultasSubprocessoService;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Serviço responsável pela finalização de processos.
 * Coordena todas as etapas necessárias para finalizar um processo,
 * incluindo validações, publicação de mapas vigentes e eventos.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ProcessoFinalizador {

    private final ProcessoRepo processoRepo;
    private final ComumRepo repo;
    private final OrganizacaoFacade organizacaoFacade;
    private final ConsultasSubprocessoService queryService;
    private final ProcessoValidador processoValidador;
    private final ProcessoNotificacaoService notificacaoService;


    /**
     * Finaliza um processo, validando e tornando os mapas vigentes.
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
        notificacaoService.emailFinalizacaoProcesso(processo.getCodigo());

        log.info("Processo {} finalizado", codigo);
    }

    /**
     * Torna os mapas de todos os subprocessos do processo como vigentes.
     */
    private void tornarMapasVigentes(Processo processo) {
        log.info("Mapa vigente definido para o processo {}", processo.getCodigo());
        List<Subprocesso> subprocessos = queryService.listarEntidadesPorProcesso(processo.getCodigo());

        for (Subprocesso subprocesso : subprocessos) {
            Unidade unidade = subprocesso.getUnidade();
            Mapa mapa = subprocesso.getMapa();
            organizacaoFacade.definirMapaVigente(unidade.getCodigo(), mapa);
        }
        log.info("Mapa(s) de {} subprocesso(s) definidos como vigentes.", subprocessos.size());
    }
}
