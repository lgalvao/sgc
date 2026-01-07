package sgc.subprocesso.service.decomposed;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.alerta.AlertaService;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.comum.erros.ErroValidacao;
import sgc.organizacao.UnidadeService;
import sgc.organizacao.model.Unidade;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.model.*;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SubprocessoWorkflowService {
    private final SubprocessoRepo repositorioSubprocesso;
    private final SubprocessoCrudService crudService;
    private final AlertaService alertaService;
    private final UnidadeService unidadeService;
    private final MovimentacaoRepo repositorioMovimentacao;

    public void alterarDataLimite(Long codSubprocesso, java.time.LocalDate novaDataLimite) {
        Subprocesso sp = crudService.buscarSubprocesso(codSubprocesso);
        SituacaoSubprocesso s = sp.getSituacao();
        int etapa = 1;

        if (s.name().contains("CADASTRO")) {
             sp.setDataLimiteEtapa1(novaDataLimite.atStartOfDay());
             etapa = 1;
        } else if (s.name().contains("MAPA")) {
             sp.setDataLimiteEtapa2(novaDataLimite.atStartOfDay());
             etapa = 2;
        } else {
             sp.setDataLimiteEtapa1(novaDataLimite.atStartOfDay());
        }

        repositorioSubprocesso.save(sp);

        try {
            String novaDataStr = novaDataLimite.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            if (sp.getUnidade() != null) {
                alertaService.criarAlertaAlteracaoDataLimite(sp.getProcesso(), sp.getUnidade(), novaDataStr, etapa);
            }
        } catch (Exception e) {
            log.error("Erro ao enviar notificações de alteração de prazo: {}", e.getMessage());
        }
    }

    public void atualizarSituacaoParaEmAndamento(Long mapaCodigo) {
        var subprocesso = repositorioSubprocesso.findByMapaCodigo(mapaCodigo)
            .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Subprocesso não encontrado para o mapa com código %d".formatted(mapaCodigo)));

        if (subprocesso.getSituacao() == SituacaoSubprocesso.NAO_INICIADO) {
            if (subprocesso.getProcesso() == null) {
                throw new ErroEntidadeNaoEncontrada("Processo não associado ao Subprocesso %d".formatted(subprocesso.getCodigo()));
            }
            var tipoProcesso = subprocesso.getProcesso().getTipo();
            if (tipoProcesso == TipoProcesso.MAPEAMENTO) {
                subprocesso.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
                repositorioSubprocesso.save(subprocesso);
            } else if (tipoProcesso == TipoProcesso.REVISAO) {
                subprocesso.setSituacao(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO);
                repositorioSubprocesso.save(subprocesso);
            }
        }
    }

    public List<Subprocesso> listarSubprocessosHomologados() {
        return repositorioSubprocesso.findBySituacao(SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA);
    }

    public void reabrirCadastro(Long codigo, String justificativa) {
        Subprocesso sp = crudService.buscarSubprocesso(codigo);

        if (sp.getProcesso().getTipo() != TipoProcesso.MAPEAMENTO) {
            throw new ErroValidacao("Reabertura de cadastro permitida apenas para processos de Mapeamento.", Map.of());
        }
        if (sp.getSituacao().ordinal() <= SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO.ordinal()) {
            throw new ErroValidacao("Subprocesso ainda está em fase de cadastro.", Map.of());
        }

        sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        sp.setDataFimEtapa1(null);
        repositorioSubprocesso.save(sp);

        registrarMovimentacaoReabertura(sp, "Reabertura de cadastro");
        enviarAlertasReabertura(sp, justificativa, false);
    }

    public void reabrirRevisaoCadastro(Long codigo, String justificativa) {
        Subprocesso sp = crudService.buscarSubprocesso(codigo);

        if (sp.getProcesso().getTipo() != TipoProcesso.REVISAO) {
            throw new ErroValidacao("Reabertura de revisão permitida apenas para processos de Revisão.", Map.of());
        }
        if (sp.getSituacao().ordinal() <= SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO.ordinal()) {
            throw new ErroValidacao("Subprocesso ainda está em fase de revisão.", Map.of());
        }

        sp.setSituacao(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO);
        sp.setDataFimEtapa1(null);
        repositorioSubprocesso.save(sp);

        registrarMovimentacaoReabertura(sp, "Reabertura de revisão de cadastro");
        enviarAlertasReabertura(sp, justificativa, true);
    }

    private void registrarMovimentacaoReabertura(Subprocesso sp, String descricao) {
        Unidade sedoc = unidadeService.buscarEntidadePorSigla("SEDOC");
        Movimentacao mov = new Movimentacao();
        mov.setSubprocesso(sp);
        mov.setDataHora(java.time.LocalDateTime.now());
        mov.setUnidadeOrigem(sedoc);
        mov.setUnidadeDestino(sp.getUnidade());
        mov.setDescricao(descricao);
        repositorioMovimentacao.save(mov);
    }

    private void enviarAlertasReabertura(Subprocesso sp, String justificativa, boolean isRevisao) {
        try {
            if (isRevisao) {
                 alertaService.criarAlertaReaberturaRevisao(sp.getProcesso(), sp.getUnidade());
            } else {
                 alertaService.criarAlertaReaberturaCadastro(sp.getProcesso(), sp.getUnidade());
            }
            Unidade superior = sp.getUnidade().getUnidadeSuperior();
            while (superior != null) {
                if (isRevisao) {
                    alertaService.criarAlertaReaberturaRevisaoSuperior(sp.getProcesso(), superior, sp.getUnidade());
                } else {
                    alertaService.criarAlertaReaberturaCadastroSuperior(sp.getProcesso(), superior, sp.getUnidade());
                }
                superior = superior.getUnidadeSuperior();
            }
        } catch (Exception e) {
            log.error("Erro ao enviar notificações de reabertura: {}", e.getMessage());
        }
    }
}
