package sgc.subprocesso.service.workflow;

import java.util.List;
import java.util.Map;

import org.jspecify.annotations.Nullable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import sgc.alerta.AlertaFacade;
import sgc.analise.AnaliseFacade;
import sgc.analise.model.TipoAcaoAnalise;
import sgc.analise.model.TipoAnalise;
import sgc.comum.erros.ErroInvarianteViolada;
import sgc.comum.erros.ErroValidacao;
import sgc.mapa.service.ImpactoMapaService;
import sgc.organizacao.UnidadeFacade;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.processo.model.TipoProcesso;
import static sgc.seguranca.acesso.Acao.ACEITAR_CADASTRO;
import static sgc.seguranca.acesso.Acao.ACEITAR_REVISAO_CADASTRO;
import static sgc.seguranca.acesso.Acao.DEVOLVER_CADASTRO;
import static sgc.seguranca.acesso.Acao.DEVOLVER_REVISAO_CADASTRO;
import static sgc.seguranca.acesso.Acao.DISPONIBILIZAR_CADASTRO;
import static sgc.seguranca.acesso.Acao.DISPONIBILIZAR_REVISAO_CADASTRO;
import static sgc.seguranca.acesso.Acao.HOMOLOGAR_CADASTRO;
import static sgc.seguranca.acesso.Acao.HOMOLOGAR_REVISAO_CADASTRO;
import sgc.seguranca.acesso.AccessControlService;
import sgc.subprocesso.eventos.TipoTransicao;
import sgc.subprocesso.model.Movimentacao;
import sgc.subprocesso.model.MovimentacaoRepo;
import sgc.subprocesso.model.SituacaoSubprocesso;
import static sgc.subprocesso.model.SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO;
import static sgc.subprocesso.model.SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO;
import static sgc.subprocesso.model.SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO;
import static sgc.subprocesso.model.SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA;
import static sgc.subprocesso.model.SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO;
import static sgc.subprocesso.model.SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA;
import static sgc.subprocesso.model.SituacaoSubprocesso.REVISAO_MAPA_HOMOLOGADO;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;
import sgc.subprocesso.service.crud.SubprocessoCrudService;
import sgc.subprocesso.service.crud.SubprocessoValidacaoService;

@Service
@Slf4j
@RequiredArgsConstructor
public class SubprocessoCadastroWorkflowService {

    private static final String SIGLA_SEDOC = "SEDOC";

    private final SubprocessoRepo subprocessoRepo;
    private final SubprocessoCrudService crudService;
    private final AlertaFacade alertaService;
    private final UnidadeFacade unidadeService;
    private final MovimentacaoRepo repositorioMovimentacao;
    private final SubprocessoTransicaoService transicaoService;
    private final AnaliseFacade analiseFacade;
    @Lazy private final SubprocessoValidacaoService validacaoService;
    @Lazy private final ImpactoMapaService impactoMapaService;
    private final AccessControlService accessControlService;

    // Métodos para reabertura de cadastro

    @Transactional
    public void reabrirCadastro(Long codigo, String justificativa) {
        Subprocesso sp = crudService.buscarSubprocesso(codigo);

        if (sp.getProcesso().getTipo() != TipoProcesso.MAPEAMENTO) {
            throw new ErroValidacao("Reabertura de cadastro permitida apenas para processos de Mapeamento.", Map.of());
        }
        if (sp.getSituacao().ordinal() <= MAPEAMENTO_CADASTRO_EM_ANDAMENTO.ordinal()) {
            throw new ErroValidacao("Subprocesso ainda está em fase de cadastro.", Map.of());
        }

        sp.setSituacao(MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        sp.setDataFimEtapa1(null);
        subprocessoRepo.save(sp);

        registrarMovimentacaoReabertura(sp, "Reabertura de cadastro");
        enviarAlertasReabertura(sp, justificativa, false);
    }

    @Transactional
    public void reabrirRevisaoCadastro(Long codigo, String justificativa) {
        Subprocesso sp = crudService.buscarSubprocesso(codigo);

        if (sp.getProcesso().getTipo() != TipoProcesso.REVISAO) {
            throw new ErroValidacao("Reabertura de revisão permitida apenas para processos de Revisão.", Map.of());
        }
        if (sp.getSituacao().ordinal() <= REVISAO_CADASTRO_EM_ANDAMENTO.ordinal()) {
            throw new ErroValidacao("Subprocesso ainda está em fase de revisão.", Map.of());
        }

        sp.setSituacao(REVISAO_CADASTRO_EM_ANDAMENTO);
        sp.setDataFimEtapa1(null);
        subprocessoRepo.save(sp);

        registrarMovimentacaoReabertura(sp, "Reabertura de revisão de cadastro");
        enviarAlertasReabertura(sp, justificativa, true);
    }

    private void registrarMovimentacaoReabertura(Subprocesso sp, String descricao) {
        Unidade sedoc = unidadeService.buscarEntidadePorSigla(SIGLA_SEDOC);
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
                alertaService.criarAlertaReaberturaRevisao(sp.getProcesso(), sp.getUnidade(), justificativa);
            } else {
                alertaService.criarAlertaReaberturaCadastro(sp.getProcesso(), sp.getUnidade(), justificativa);
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

    // Métodos de workflow de cadastro

    @Transactional
    public void disponibilizarCadastro(Long codSubprocesso, Usuario usuario) {
        disponibilizar(codSubprocesso, usuario, DISPONIBILIZAR_CADASTRO, MAPEAMENTO_CADASTRO_DISPONIBILIZADO,
                TipoTransicao.CADASTRO_DISPONIBILIZADO);
    }

    @Transactional
    public void disponibilizarRevisao(Long codSubprocesso, Usuario usuario) {
        disponibilizar(codSubprocesso, usuario, DISPONIBILIZAR_REVISAO_CADASTRO, REVISAO_CADASTRO_DISPONIBILIZADA,
                TipoTransicao.REVISAO_CADASTRO_DISPONIBILIZADA);
    }

    private void disponibilizar(Long codSubprocesso, Usuario usuario, sgc.seguranca.acesso.Acao acao,
            SituacaoSubprocesso novaSituacao, TipoTransicao transicao) {
        Subprocesso sp = crudService.buscarSubprocesso(codSubprocesso);
        accessControlService.verificarPermissao(usuario, acao, sp);
        validarRequisitosNegocioParaDisponibilizacao(codSubprocesso);

        Unidade origem = sp.getUnidade();
        Unidade destino = origem.getUnidadeSuperior();

        sp.setSituacao(novaSituacao);
        sp.setDataFimEtapa1(java.time.LocalDateTime.now());
        subprocessoRepo.save(sp);

        analiseFacade.removerPorSubprocesso(sp.getCodigo());
        transicaoService.registrar(sp, transicao, origem, destino, usuario);
    }

    private void validarRequisitosNegocioParaDisponibilizacao(Long codSubprocesso) {
        validacaoService.validarExistenciaAtividades(codSubprocesso);

        if (!validacaoService.obterAtividadesSemConhecimento(codSubprocesso).isEmpty()) {
            throw new ErroValidacao("Existem atividades sem conhecimentos associados.");
        }
    }

    @Transactional
    public void devolverCadastro(Long codSubprocesso, @Nullable String observacoes, Usuario usuario) {
        Subprocesso sp = crudService.buscarSubprocesso(codSubprocesso);
        accessControlService.verificarPermissao(usuario, DEVOLVER_CADASTRO, sp);

        Unidade unidadeSubprocesso = sp.getUnidade();
        Unidade unidadeAnalise = unidadeSubprocesso.getUnidadeSuperior();
        if (unidadeAnalise == null) {
            throw new ErroInvarianteViolada("Unidade superior não encontrada para o subprocesso " + codSubprocesso);
        }

        sp.setDataFimEtapa1(null);
        transicaoService.registrarAnaliseETransicao(new SubprocessoTransicaoService.RegistrarWorkflowReq(
                sp,
                MAPEAMENTO_CADASTRO_EM_ANDAMENTO,
                TipoTransicao.CADASTRO_DEVOLVIDO,
                TipoAnalise.CADASTRO,
                TipoAcaoAnalise.DEVOLUCAO_MAPEAMENTO,
                unidadeAnalise,
                unidadeAnalise,
                sp.getUnidade(),
                usuario,
                observacoes,
                null));
    }

    @Transactional
    public void aceitarCadastro(Long codSubprocesso, @Nullable String observacoes, Usuario usuario) {
        Subprocesso sp = crudService.buscarSubprocesso(codSubprocesso);
        accessControlService.verificarPermissao(usuario, ACEITAR_CADASTRO, sp);

        Unidade unidadeOrigem = sp.getUnidade();
        Unidade unidadeDestino = unidadeOrigem.getUnidadeSuperior();
        if (unidadeDestino == null) {
            throw new ErroInvarianteViolada("Não foi possível identificar a unidade superior para enviar a análise.");
        }

        transicaoService.registrarAnaliseETransicao(new SubprocessoTransicaoService.RegistrarWorkflowReq(
                sp,
                MAPEAMENTO_CADASTRO_DISPONIBILIZADO,
                TipoTransicao.CADASTRO_ACEITO,
                TipoAnalise.CADASTRO,
                TipoAcaoAnalise.ACEITE_MAPEAMENTO,
                unidadeDestino,
                unidadeOrigem,
                unidadeDestino,
                usuario,
                observacoes,
                null));
    }

    @Transactional
    public void homologarCadastro(Long codSubprocesso, @Nullable String observacoes, Usuario usuario) {
        Subprocesso sp = crudService.buscarSubprocesso(codSubprocesso);
        accessControlService.verificarPermissao(usuario, HOMOLOGAR_CADASTRO, sp);

        Unidade sedoc = unidadeService.buscarEntidadePorSigla(SIGLA_SEDOC);
        sp.setSituacao(MAPEAMENTO_CADASTRO_HOMOLOGADO);
        subprocessoRepo.save(sp);

        transicaoService.registrar(sp, TipoTransicao.CADASTRO_HOMOLOGADO, sedoc, sedoc, usuario, observacoes);
    }

    @Transactional
    public void devolverRevisaoCadastro(Long codSubprocesso, @Nullable String observacoes, Usuario usuario) {
        Subprocesso sp = crudService.buscarSubprocesso(codSubprocesso);
        accessControlService.verificarPermissao(usuario, DEVOLVER_REVISAO_CADASTRO, sp);

        Unidade unidadeSubprocesso = sp.getUnidade();
        Unidade unidadeAnalise = unidadeSubprocesso.getUnidadeSuperior();
        if (unidadeAnalise == null) {
            throw new ErroInvarianteViolada("Unidade superior não encontrada para o subprocesso " + codSubprocesso);
        }

        sp.setDataFimEtapa1(null);
        transicaoService.registrarAnaliseETransicao(new SubprocessoTransicaoService.RegistrarWorkflowReq(
                sp,
                REVISAO_CADASTRO_EM_ANDAMENTO,
                TipoTransicao.REVISAO_CADASTRO_DEVOLVIDA,
                TipoAnalise.CADASTRO,
                TipoAcaoAnalise.DEVOLUCAO_REVISAO,
                unidadeAnalise,
                unidadeAnalise,
                sp.getUnidade(),
                usuario,
                observacoes,
                null));
    }

    @Transactional
    public void aceitarRevisaoCadastro(Long codSubprocesso, @Nullable String observacoes, Usuario usuario) {
        Subprocesso sp = crudService.buscarSubprocesso(codSubprocesso);
        accessControlService.verificarPermissao(usuario, ACEITAR_REVISAO_CADASTRO, sp);

        Unidade unidadeSubprocesso = sp.getUnidade();
        Unidade unidadeAnalise = unidadeSubprocesso.getUnidadeSuperior();
        if (unidadeAnalise == null) {
            throw new ErroInvarianteViolada("Unidade superior não encontrada para o subprocesso " + codSubprocesso);
        }

        Unidade unidadeDestino = unidadeAnalise.getUnidadeSuperior() != null ? unidadeAnalise.getUnidadeSuperior()
                : unidadeAnalise;

        transicaoService.registrarAnaliseETransicao(new SubprocessoTransicaoService.RegistrarWorkflowReq(
                sp,
                REVISAO_CADASTRO_DISPONIBILIZADA,
                TipoTransicao.REVISAO_CADASTRO_ACEITA,
                TipoAnalise.CADASTRO,
                TipoAcaoAnalise.ACEITE_REVISAO,
                unidadeAnalise,
                unidadeAnalise,
                unidadeDestino,
                usuario,
                observacoes,
                null));
    }

    @Transactional
    public void homologarRevisaoCadastro(Long codSubprocesso, @Nullable String observacoes, Usuario usuario) {
        Subprocesso sp = crudService.buscarSubprocesso(codSubprocesso);
        accessControlService.verificarPermissao(usuario, HOMOLOGAR_REVISAO_CADASTRO, sp);

        var impactos = impactoMapaService.verificarImpactos(sp, usuario);
        if (impactos.temImpactos()) {
            Unidade sedoc = unidadeService.buscarEntidadePorSigla(SIGLA_SEDOC);
            sp.setSituacao(REVISAO_CADASTRO_HOMOLOGADA);
            subprocessoRepo.save(sp);
            transicaoService.registrar(sp, TipoTransicao.REVISAO_CADASTRO_HOMOLOGADA, sedoc, sedoc, usuario,
                    observacoes);
        } else {
            sp.setSituacao(REVISAO_MAPA_HOMOLOGADO);
            subprocessoRepo.save(sp);
        }
    }

    @Transactional
    public void aceitarCadastroEmBloco(List<Long> subprocessoCodigos, Usuario usuario) {
        subprocessoCodigos.forEach(codSubprocesso -> aceitarCadastro(codSubprocesso, "De acordo com o cadastro de atividades da unidade (Em Bloco)", usuario));
    }

    @Transactional
    public void homologarCadastroEmBloco(List<Long> subprocessoCodigos, Usuario usuario) {
        subprocessoCodigos.forEach(codSubprocesso -> homologarCadastro(codSubprocesso, "Homologação em bloco", usuario));
    }
}
