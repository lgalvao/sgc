package sgc.subprocesso.service.workflow;

import java.util.List;
import java.util.Map;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import sgc.alerta.AlertaFacade;
import sgc.analise.AnaliseFacade;
import sgc.analise.model.TipoAcaoAnalise;
import sgc.analise.model.TipoAnalise;
import sgc.comum.erros.ErroValidacao;
import sgc.mapa.service.ImpactoMapaService;
import sgc.organizacao.UnidadeFacade;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import static sgc.seguranca.acesso.Acao.ACEITAR_CADASTRO;
import static sgc.seguranca.acesso.Acao.ACEITAR_REVISAO_CADASTRO;
import static sgc.seguranca.acesso.Acao.DEVOLVER_CADASTRO;
import static sgc.seguranca.acesso.Acao.DEVOLVER_REVISAO_CADASTRO;
import static sgc.seguranca.acesso.Acao.DISPONIBILIZAR_CADASTRO;
import static sgc.seguranca.acesso.Acao.DISPONIBILIZAR_REVISAO_CADASTRO;
import static sgc.seguranca.acesso.Acao.HOMOLOGAR_CADASTRO;
import static sgc.seguranca.acesso.Acao.HOMOLOGAR_REVISAO_CADASTRO;

import sgc.seguranca.acesso.Acao;
import sgc.seguranca.acesso.AccessControlService;
import sgc.subprocesso.dto.RegistrarTransicaoCommand;
import sgc.subprocesso.dto.RegistrarWorkflowCommand;
import sgc.subprocesso.eventos.TipoTransicao;
import sgc.subprocesso.model.SituacaoSubprocesso;
import static sgc.subprocesso.model.SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO;
import static sgc.subprocesso.model.SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO;
import static sgc.subprocesso.model.SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO;
import static sgc.subprocesso.model.SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA;
import static sgc.subprocesso.model.SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO;
import static sgc.subprocesso.model.SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA;
import static sgc.subprocesso.model.SituacaoSubprocesso.REVISAO_MAPA_HOMOLOGADO;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.crud.SubprocessoCrudService;
import sgc.subprocesso.service.crud.SubprocessoValidacaoService;
import java.time.LocalDateTime;
import sgc.subprocesso.model.SubprocessoRepo;

@Service
@Slf4j
@RequiredArgsConstructor
public class SubprocessoCadastroWorkflowService {
    private static final String SIGLA_SEDOC = "SEDOC";

    private final SubprocessoRepo subprocessoRepo;
    private final SubprocessoCrudService crudService;
    private final AlertaFacade alertaService;
    private final UnidadeFacade unidadeService;
    private final SubprocessoTransicaoService transicaoService;
    private final AnaliseFacade analiseFacade;
    private final UsuarioFacade usuarioServiceFacade;

    private final SubprocessoValidacaoService validacaoService;
    private final ImpactoMapaService impactoMapaService;

    private final AccessControlService accessControlService;

    // Métodos para reabertura de cadastro

    @Transactional
    public void reabrirCadastro(Long codigo, String justificativa) {
        Subprocesso sp = crudService.buscarSubprocesso(codigo);

        if (sp.getSituacao().ordinal() < MAPEAMENTO_CADASTRO_HOMOLOGADO.ordinal()) {
            throw new ErroValidacao("Subprocesso ainda está em fase de cadastro.", Map.of());
        }

        Unidade sedoc = unidadeService.buscarEntidadePorSigla(SIGLA_SEDOC);
        Usuario usuario = usuarioServiceFacade.obterUsuarioAutenticadoOuNull();

        sp.setSituacao(MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        sp.setDataFimEtapa1(null);
        subprocessoRepo.save(sp);

        transicaoService.registrar(RegistrarTransicaoCommand.builder()
                .sp(sp)
                .tipo(TipoTransicao.CADASTRO_REABERTO)
                .origem(sedoc)
                .destino(sp.getUnidade())
                .usuario(usuario)
                .observacoes(justificativa)
                .build());

        enviarAlertasReabertura(sp, justificativa, false);
    }

    @Transactional
    public void reabrirRevisaoCadastro(Long codigo, String justificativa) {
        Subprocesso sp = crudService.buscarSubprocesso(codigo);

        if (sp.getSituacao().ordinal() < REVISAO_CADASTRO_HOMOLOGADA.ordinal()) {
            throw new ErroValidacao("Subprocesso ainda está em fase de revisão.", Map.of());
        }

        Unidade sedoc = unidadeService.buscarEntidadePorSigla(SIGLA_SEDOC);
        Usuario usuario = usuarioServiceFacade.obterUsuarioAutenticadoOuNull();

        sp.setSituacao(REVISAO_CADASTRO_EM_ANDAMENTO);
        sp.setDataFimEtapa1(null);
        subprocessoRepo.save(sp);

        transicaoService.registrar(RegistrarTransicaoCommand.builder()
                .sp(sp)
                .tipo(TipoTransicao.REVISAO_CADASTRO_REABERTA)
                .origem(sedoc)
                .destino(sp.getUnidade())
                .usuario(usuario)
                .observacoes(justificativa)
                .build());

        enviarAlertasReabertura(sp, justificativa, true);
    }



    private void enviarAlertasReabertura(Subprocesso sp, String justificativa, boolean isRevisao) {
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
    }

    // Métodos de workflow de cadastro

    @Transactional
    public void disponibilizarCadastro(Long codSubprocesso, Usuario usuario) {
        disponibilizar(codSubprocesso, MAPEAMENTO_CADASTRO_DISPONIBILIZADO, TipoTransicao.CADASTRO_DISPONIBILIZADO,
                DISPONIBILIZAR_CADASTRO, usuario);
    }

    @Transactional
    public void disponibilizarRevisao(Long codSubprocesso, Usuario usuario) {
        disponibilizar(codSubprocesso, REVISAO_CADASTRO_DISPONIBILIZADA,
                TipoTransicao.REVISAO_CADASTRO_DISPONIBILIZADA, DISPONIBILIZAR_REVISAO_CADASTRO, usuario);
    }

    private void disponibilizar(Long codSubprocesso, SituacaoSubprocesso novaSituacao,
            TipoTransicao transicao, Acao acaoPermissao, Usuario usuario) {

        Subprocesso sp = crudService.buscarSubprocesso(codSubprocesso);
        accessControlService.verificarPermissao(usuario, acaoPermissao, sp);
        validarRequisitosNegocioParaDisponibilizacao(codSubprocesso);

        Unidade origem = sp.getUnidade();
        if (origem == null) {
            throw new IllegalStateException("Subprocesso sem unidade vinculada: " + codSubprocesso);
        }

        Unidade destino = origem.getUnidadeSuperior();
        if (destino == null) {
            log.warn("Unidade {} não possui superior. Usando a própria unidade como destino.", origem.getSigla());
            destino = origem;
        }

        sp.setSituacao(novaSituacao);
        sp.setDataFimEtapa1(LocalDateTime.now());
        subprocessoRepo.save(sp);

        analiseFacade.removerPorSubprocesso(sp.getCodigo());
        
        final Unidade destinoFinal = destino; // Effectively final for builder if needed
        transicaoService.registrar(RegistrarTransicaoCommand.builder()
                .sp(sp)
                .tipo(transicao)
                .origem(origem)
                .destino(destinoFinal)
                .usuario(usuario)
                .build());
    }

    private void validarRequisitosNegocioParaDisponibilizacao(Long codSubprocesso) {
        validacaoService.validarExistenciaAtividades(codSubprocesso);

        if (!validacaoService.obterAtividadesSemConhecimento(codSubprocesso).isEmpty()) {
            throw new ErroValidacao("Existem atividades sem conhecimentos associados.");
        }
    }

    @Transactional
    public void devolverCadastro(Long codSubprocesso, Usuario usuario, @Nullable String observacoes) {
        Subprocesso sp = crudService.buscarSubprocesso(codSubprocesso);
        accessControlService.verificarPermissao(usuario, DEVOLVER_CADASTRO, sp);

        Unidade unidadeSubprocesso = sp.getUnidade();
        Unidade unidadeAnalise = unidadeSubprocesso.getUnidadeSuperior();

        if (unidadeAnalise == null) {
            unidadeAnalise = unidadeSubprocesso;
        }

        sp.setDataFimEtapa1(null);
        transicaoService.registrarAnaliseETransicao(RegistrarWorkflowCommand.builder()
                .sp(sp)
                .novaSituacao(MAPEAMENTO_CADASTRO_EM_ANDAMENTO)
                .tipoTransicao(TipoTransicao.CADASTRO_DEVOLVIDO)
                .tipoAnalise(TipoAnalise.CADASTRO)
                .tipoAcaoAnalise(TipoAcaoAnalise.DEVOLUCAO_MAPEAMENTO)
                .unidadeAnalise(unidadeAnalise)
                .unidadeOrigemTransicao(unidadeAnalise)
                .unidadeDestinoTransicao(sp.getUnidade())
                .usuario(usuario)
                .motivoAnalise(observacoes)
                .observacoes(observacoes)
                .build());
    }

    @Transactional
    public void aceitarCadastro(Long codSubprocesso, Usuario usuario, @Nullable String observacoes) {
        executarAceiteCadastro(codSubprocesso, usuario, observacoes);
    }

    private void executarAceiteCadastro(Long codSubprocesso, Usuario usuario, @Nullable String observacoes) {
        Subprocesso sp = crudService.buscarSubprocesso(codSubprocesso);
        accessControlService.verificarPermissao(usuario, ACEITAR_CADASTRO, sp);

        Unidade unidadeOrigem = sp.getUnidade();
        Unidade unidadeDestino = unidadeOrigem.getUnidadeSuperior();
        if (unidadeDestino == null) {
            unidadeDestino = unidadeOrigem;
        }

        transicaoService.registrarAnaliseETransicao(RegistrarWorkflowCommand.builder()
                .sp(sp)
                .novaSituacao(MAPEAMENTO_CADASTRO_DISPONIBILIZADO)
                .tipoTransicao(TipoTransicao.CADASTRO_ACEITO)
                .tipoAnalise(TipoAnalise.CADASTRO)
                .tipoAcaoAnalise(TipoAcaoAnalise.ACEITE_MAPEAMENTO)
                .unidadeAnalise(unidadeDestino)
                .unidadeOrigemTransicao(unidadeOrigem)
                .unidadeDestinoTransicao(unidadeDestino)
                .usuario(usuario)
                .motivoAnalise(observacoes)
                .observacoes(observacoes)
                .build());
    }

    @Transactional
    public void homologarCadastro(Long codSubprocesso, Usuario usuario, @Nullable String observacoes) {
        executarHomologacaoCadastro(codSubprocesso, usuario, observacoes);
    }

    private void executarHomologacaoCadastro(Long codSubprocesso, Usuario usuario, @Nullable String observacoes) {
        Subprocesso sp = crudService.buscarSubprocesso(codSubprocesso);
        accessControlService.verificarPermissao(usuario, HOMOLOGAR_CADASTRO, sp);

        Unidade sedoc = unidadeService.buscarEntidadePorSigla(SIGLA_SEDOC);
        sp.setSituacao(MAPEAMENTO_CADASTRO_HOMOLOGADO);
        subprocessoRepo.save(sp);

        transicaoService.registrar(RegistrarTransicaoCommand.builder()
                .sp(sp)
                .tipo(TipoTransicao.CADASTRO_HOMOLOGADO)
                .origem(sedoc)
                .destino(sedoc)
                .usuario(usuario)
                .observacoes(observacoes)
                .build());
    }

    @Transactional
    public void devolverRevisaoCadastro(Long codSubprocesso, Usuario usuario, @Nullable String observacoes) {
        Subprocesso sp = crudService.buscarSubprocesso(codSubprocesso);
        accessControlService.verificarPermissao(usuario, DEVOLVER_REVISAO_CADASTRO, sp);

        Unidade unidadeSubprocesso = sp.getUnidade();
        Unidade unidadeAnalise = unidadeSubprocesso.getUnidadeSuperior();
        if (unidadeAnalise == null) {
            unidadeAnalise = unidadeSubprocesso;
        }

        sp.setDataFimEtapa1(null);
        transicaoService.registrarAnaliseETransicao(RegistrarWorkflowCommand.builder()
                .sp(sp)
                .novaSituacao(REVISAO_CADASTRO_EM_ANDAMENTO)
                .tipoTransicao(TipoTransicao.REVISAO_CADASTRO_DEVOLVIDA)
                .tipoAnalise(TipoAnalise.CADASTRO)
                .tipoAcaoAnalise(TipoAcaoAnalise.DEVOLUCAO_REVISAO)
                .unidadeAnalise(unidadeAnalise)
                .unidadeOrigemTransicao(unidadeAnalise)
                .unidadeDestinoTransicao(sp.getUnidade())
                .usuario(usuario)
                .motivoAnalise(observacoes)
                .observacoes(observacoes)
                .build());
    }

    @Transactional
    public void aceitarRevisaoCadastro(Long codSubprocesso, Usuario usuario, @Nullable String observacoes) {
        Subprocesso sp = crudService.buscarSubprocesso(codSubprocesso);
        accessControlService.verificarPermissao(usuario, ACEITAR_REVISAO_CADASTRO, sp);

        Unidade unidadeSubprocesso = sp.getUnidade();
        Unidade unidadeAnalise = unidadeSubprocesso.getUnidadeSuperior();
        if (unidadeAnalise == null) {
            unidadeAnalise = unidadeSubprocesso;
        }

        Unidade superiorAnalise = unidadeAnalise.getUnidadeSuperior();
        Unidade unidadeDestino = (superiorAnalise != null) ? superiorAnalise : unidadeAnalise;

        transicaoService.registrarAnaliseETransicao(RegistrarWorkflowCommand.builder()
                .sp(sp)
                .novaSituacao(REVISAO_CADASTRO_DISPONIBILIZADA)
                .tipoTransicao(TipoTransicao.REVISAO_CADASTRO_ACEITA)
                .tipoAnalise(TipoAnalise.CADASTRO)
                .tipoAcaoAnalise(TipoAcaoAnalise.ACEITE_REVISAO)
                .unidadeAnalise(unidadeAnalise)
                .unidadeOrigemTransicao(unidadeAnalise)
                .unidadeDestinoTransicao(unidadeDestino)
                .usuario(usuario)
                .motivoAnalise(observacoes)
                .observacoes(observacoes)
                .build());
    }

    @Transactional
    public void homologarRevisaoCadastro(Long codSubprocesso, Usuario usuario, @Nullable String observacoes) {
        Subprocesso sp = crudService.buscarSubprocesso(codSubprocesso);
        accessControlService.verificarPermissao(usuario, HOMOLOGAR_REVISAO_CADASTRO, sp);

        var impactos = impactoMapaService.verificarImpactos(sp, usuario);
        if (impactos.temImpactos()) {
            Unidade sedoc = unidadeService.buscarEntidadePorSigla(SIGLA_SEDOC);
            sp.setSituacao(REVISAO_CADASTRO_HOMOLOGADA);
            subprocessoRepo.save(sp);
            transicaoService.registrar(RegistrarTransicaoCommand.builder()
                    .sp(sp)
                    .tipo(TipoTransicao.REVISAO_CADASTRO_HOMOLOGADA)
                    .origem(sedoc)
                    .destino(sedoc)
                    .usuario(usuario)
                    .observacoes(observacoes)
                    .build());
        } else {
            sp.setSituacao(REVISAO_MAPA_HOMOLOGADO);
            subprocessoRepo.save(sp);
        }
    }

    @Transactional
    public void aceitarCadastroEmBloco(List<Long> subprocessoCodigos, Usuario usuario) {
        subprocessoCodigos.forEach(codSubprocesso -> executarAceiteCadastro(codSubprocesso, usuario,
                "De acordo com o cadastro de atividades da unidade (Em Bloco)"));
    }

    @Transactional
    public void homologarCadastroEmBloco(List<Long> subprocessoCodigos, Usuario usuario) {
        subprocessoCodigos.forEach(
                codSubprocesso -> executarHomologacaoCadastro(codSubprocesso, usuario, "Homologação em bloco"));
    }
}
