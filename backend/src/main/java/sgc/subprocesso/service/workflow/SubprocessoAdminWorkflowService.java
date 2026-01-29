package sgc.subprocesso.service.workflow;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import sgc.alerta.AlertaFacade;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.model.SituacaoSubprocesso;
import static sgc.subprocesso.model.SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO;
import static sgc.subprocesso.model.SituacaoSubprocesso.NAO_INICIADO;
import static sgc.subprocesso.model.SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO;
import static sgc.subprocesso.model.SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.SubprocessoRepositoryService;
import sgc.subprocesso.service.crud.SubprocessoCrudService;

@Service
@Slf4j
@RequiredArgsConstructor
public class SubprocessoAdminWorkflowService {

    private final SubprocessoRepositoryService subprocessoService;
    private final SubprocessoCrudService crudService;
    private final AlertaFacade alertaService;

    @Transactional
    public void alterarDataLimite(Long codSubprocesso, java.time.LocalDate novaDataLimite) {
        Subprocesso sp = crudService.buscarSubprocesso(codSubprocesso);
        SituacaoSubprocesso s = sp.getSituacao();
        int etapa = 1;

        if (s.name().contains("CADASTRO")) {
            sp.setDataLimiteEtapa1(novaDataLimite.atStartOfDay());
        } else if (s.name().contains("MAPA")) {
            sp.setDataLimiteEtapa2(novaDataLimite.atStartOfDay());
            etapa = 2;
        } else {
            sp.setDataLimiteEtapa1(novaDataLimite.atStartOfDay());
        }

        subprocessoService.save(sp);

        try {
            String novaDataStr = novaDataLimite.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            alertaService.criarAlertaAlteracaoDataLimite(sp.getProcesso(), sp.getUnidade(), novaDataStr, etapa);
        } catch (Exception e) {
            log.error("Erro ao enviar notificações de alteração de prazo: {}", e.getMessage());
        }
    }

    @Transactional
    public void atualizarSituacaoParaEmAndamento(Long mapaCodigo) {
        var subprocesso = subprocessoService.findByMapaCodigo(mapaCodigo)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Subprocesso do mapa", mapaCodigo));

        if (subprocesso.getSituacao() == NAO_INICIADO) {
            var tipoProcesso = subprocesso.getProcesso().getTipo();
            if (tipoProcesso == TipoProcesso.MAPEAMENTO) {
                subprocesso.setSituacao(MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
                subprocessoService.save(subprocesso);
            } else if (tipoProcesso == TipoProcesso.REVISAO) {
                subprocesso.setSituacao(REVISAO_CADASTRO_EM_ANDAMENTO);
                subprocessoService.save(subprocesso);
            }
        }
    }

    public List<Subprocesso> listarSubprocessosHomologados() {
        return subprocessoService.findBySituacao(REVISAO_CADASTRO_HOMOLOGADA);
    }
}
