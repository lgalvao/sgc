package sgc.subprocesso.service.workflow;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.alerta.AlertaFacade;
import sgc.comum.repo.ComumRepo;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;
import sgc.subprocesso.service.crud.SubprocessoCrudService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static sgc.subprocesso.model.SituacaoSubprocesso.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class SubprocessoAdminWorkflowService {

    private final SubprocessoRepo subprocessoRepo;
    private final ComumRepo repo;
    private final SubprocessoCrudService crudService;
    private final AlertaFacade alertaService;

    @Transactional
    public void alterarDataLimite(Long codSubprocesso, LocalDate novaDataLimite) {
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

        subprocessoRepo.save(sp);

        try {
            String novaDataStr = novaDataLimite.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            alertaService.criarAlertaAlteracaoDataLimite(sp.getProcesso(), sp.getUnidade(), novaDataStr, etapa);
        } catch (Exception e) {
            log.error("Erro ao enviar notificações de alteração de prazo: {}", e.getMessage());
        }
    }

    @Transactional
    public void atualizarSituacaoParaEmAndamento(Long mapaCodigo) {
        var subprocesso = repo.buscar(Subprocesso.class, "mapa.codigo", mapaCodigo);

        if (subprocesso.getSituacao() == NAO_INICIADO) {
            var tipoProcesso = subprocesso.getProcesso().getTipo();
            if (tipoProcesso == TipoProcesso.MAPEAMENTO) {
                log.info("Atualizando situação do subprocesso {} para MAPEAMENTO_CADASTRO_EM_ANDAMENTO", subprocesso.getCodigo());
                subprocesso.setSituacao(MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
                subprocessoRepo.save(subprocesso);
            } else if (tipoProcesso == TipoProcesso.REVISAO) {
                log.info("Atualizando situação do subprocesso {} para REVISAO_CADASTRO_EM_ANDAMENTO", subprocesso.getCodigo());
                subprocesso.setSituacao(REVISAO_CADASTRO_EM_ANDAMENTO);
                subprocessoRepo.save(subprocesso);
            }
        }
    }

    public List<Subprocesso> listarSubprocessosHomologados() {
        return subprocessoRepo.findBySituacao(REVISAO_CADASTRO_HOMOLOGADA);
    }
}
