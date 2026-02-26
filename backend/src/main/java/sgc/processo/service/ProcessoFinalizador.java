package sgc.processo.service;

import lombok.*;
import lombok.extern.slf4j.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import sgc.comum.model.*;
import sgc.mapa.model.*;
import sgc.organizacao.*;
import sgc.organizacao.model.*;
import sgc.processo.model.*;
import sgc.subprocesso.model.*;
import sgc.subprocesso.service.*;

import java.time.*;
import java.util.*;

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
