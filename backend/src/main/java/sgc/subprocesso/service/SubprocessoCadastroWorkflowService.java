package sgc.subprocesso.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.analise.AnaliseService;
import sgc.analise.model.TipoAcaoAnalise;
import sgc.analise.model.TipoAnalise;
import sgc.comum.erros.ErroAccessoNegado;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.comum.erros.ErroInvarianteViolada;
import sgc.comum.erros.ErroValidacao;
import sgc.mapa.model.Mapa;
import sgc.mapa.service.ImpactoMapaService;
import sgc.organizacao.UnidadeService;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.processo.erros.ErroProcessoEmSituacaoInvalida;
import sgc.seguranca.acesso.AccessControlService;
import sgc.subprocesso.erros.ErroMapaNaoAssociado;
import sgc.subprocesso.eventos.TipoTransicao;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;

import static sgc.seguranca.acesso.Acao.*;
import static sgc.subprocesso.model.SituacaoSubprocesso.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubprocessoCadastroWorkflowService {
    private final SubprocessoRepo repositorioSubprocesso;
    private final SubprocessoTransicaoService transicaoService;
    private final UnidadeService unidadeService;
    private final AnaliseService analiseService;
    private final SubprocessoService subprocessoService;
    private final ImpactoMapaService impactoMapaService;
    private final AccessControlService accessControlService;

    @Transactional
    public void disponibilizarCadastro(Long codSubprocesso, Usuario usuario) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);

        // Verificação centralizada de acesso
        accessControlService.verificarPermissao(usuario, DISPONIBILIZAR_CADASTRO, sp);
        
        // Validações de negócio (sem verificações de acesso)
        validarRequisitosNegocioParaDisponibilizacao(codSubprocesso, sp);
        
        Unidade origem = sp.getUnidade();
        Unidade destino = origem != null ? origem.getUnidadeSuperior() : null;
        
        sp.setSituacao(MAPEAMENTO_CADASTRO_DISPONIBILIZADO);
        sp.setDataFimEtapa1(java.time.LocalDateTime.now());
        repositorioSubprocesso.save(sp);

        analiseService.removerPorSubprocesso(sp.getCodigo());

        transicaoService.registrar(
                sp,
                TipoTransicao.CADASTRO_DISPONIBILIZADO,
                origem,
                destino,
                usuario);
    }

    @Transactional
    public void disponibilizarRevisao(Long codSubprocesso, Usuario usuario) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);

        // Verificação centralizada de acesso
        accessControlService.verificarPermissao(usuario, DISPONIBILIZAR_REVISAO_CADASTRO, sp);
        
        // Validações de negócio (sem verificações de acesso)
        validarRequisitosNegocioParaDisponibilizacao(codSubprocesso, sp);
        
        Unidade origem = sp.getUnidade();
        Unidade destino = origem != null ? origem.getUnidadeSuperior() : null;
        
        sp.setSituacao(REVISAO_CADASTRO_DISPONIBILIZADA);
        sp.setDataFimEtapa1(java.time.LocalDateTime.now());
        repositorioSubprocesso.save(sp);

        analiseService.removerPorSubprocesso(sp.getCodigo());

        transicaoService.registrar(
                sp,
                TipoTransicao.REVISAO_CADASTRO_DISPONIBILIZADA,
                origem,
                destino,
                usuario);
    }

    /**
     * Valida requisitos de negócio para disponibilização (SEM verificações de acesso).
     * As verificações de acesso devem ser feitas via AccessControlService.
     */
    private void validarRequisitosNegocioParaDisponibilizacao(Long codSubprocesso, Subprocesso sp) {
        // Valida se há pelo menos uma atividade cadastrada
        subprocessoService.validarExistenciaAtividades(codSubprocesso);

        // Valida se todas as atividades têm conhecimentos associados
        if (!subprocessoService.obterAtividadesSemConhecimento(codSubprocesso).isEmpty()) {
            throw new ErroValidacao("Existem atividades sem conhecimentos associados.");
        }

        Mapa mapa = sp.getMapa();
        if (mapa == null || mapa.getCodigo() == null) {
            throw new ErroMapaNaoAssociado("Subprocesso sem mapa associado");
        }
    }

    private final SubprocessoWorkflowExecutor workflowExecutor;

    @Transactional
    public void devolverCadastro(Long codSubprocesso, String observacoes, Usuario usuario) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);

        // Verificação centralizada de acesso
        accessControlService.verificarPermissao(usuario, DEVOLVER_CADASTRO, sp);

        Unidade unidadeAnalise = sp.getUnidade().getUnidadeSuperior();
        if (unidadeAnalise == null) {
            throw new ErroInvarianteViolada(
                    "Unidade superior não encontrada para o subprocesso " + codSubprocesso);
        }

        sp.setDataFimEtapa1(null);

        workflowExecutor.registrarAnaliseETransicao(
                sp,
                MAPEAMENTO_CADASTRO_EM_ANDAMENTO,
                TipoTransicao.CADASTRO_DEVOLVIDO,
                TipoAnalise.CADASTRO,
                TipoAcaoAnalise.DEVOLUCAO_MAPEAMENTO,
                unidadeAnalise,
                unidadeAnalise, // Origin: Superior
                sp.getUnidade(), // Destination: Operational
                usuario,
                observacoes,
                null
        );
    }

    @Transactional
    public void aceitarCadastro(Long codSubprocesso, String observacoes, Usuario usuario) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);

        // Verificação centralizada de acesso
        accessControlService.verificarPermissao(usuario, ACEITAR_CADASTRO, sp);

        Unidade unidadeOrigem = sp.getUnidade();
        Unidade unidadeDestino = unidadeOrigem.getUnidadeSuperior();
        if (unidadeDestino == null) {
            throw new ErroInvarianteViolada(
                    "Não foi possível identificar a unidade superior para enviar a análise.");
        }

        workflowExecutor.registrarAnaliseETransicao(
                sp,
                MAPEAMENTO_CADASTRO_DISPONIBILIZADO,
                TipoTransicao.CADASTRO_ACEITO,
                TipoAnalise.CADASTRO,
                TipoAcaoAnalise.ACEITE_MAPEAMENTO,
                unidadeDestino,
                unidadeOrigem, // Origin: Operational
                unidadeDestino, // Destination: Superior
                usuario,
                observacoes,
                null
        );
    }

    @Transactional
    public void homologarCadastro(Long codSubprocesso, String observacoes, Usuario usuario) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);

        // Verificação centralizada de acesso (já verifica situação)
        accessControlService.verificarPermissao(usuario, HOMOLOGAR_CADASTRO, sp);

        Unidade sedoc = unidadeService.buscarEntidadePorSigla("SEDOC");

        sp.setSituacao(MAPEAMENTO_CADASTRO_HOMOLOGADO);
        repositorioSubprocesso.save(sp);

        transicaoService.registrar(
                sp,
                TipoTransicao.CADASTRO_HOMOLOGADO,
                sedoc,
                sedoc,
                usuario,
                observacoes);
    }

    @Transactional
    public void devolverRevisaoCadastro(Long codSubprocesso, String observacoes, Usuario usuario) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);

        // Verificação centralizada de acesso (já verifica situação)
        accessControlService.verificarPermissao(usuario, DEVOLVER_REVISAO_CADASTRO, sp);

        Unidade unidadeAnalise = sp.getUnidade().getUnidadeSuperior();
        if (unidadeAnalise == null) {
            throw new ErroInvarianteViolada(
                    "Unidade superior não encontrada para o subprocesso " + codSubprocesso);
        }

        sp.setDataFimEtapa1(null);

        workflowExecutor.registrarAnaliseETransicao(
                sp,
                REVISAO_CADASTRO_EM_ANDAMENTO,
                TipoTransicao.REVISAO_CADASTRO_DEVOLVIDA,
                TipoAnalise.CADASTRO,
                TipoAcaoAnalise.DEVOLUCAO_REVISAO,
                unidadeAnalise,
                unidadeAnalise, // Origin: Superior
                sp.getUnidade(), // Destination: Operational
                usuario,
                observacoes,
                null
        );
    }

    @Transactional
    public void aceitarRevisaoCadastro(Long codSubprocesso, String observacoes, Usuario usuario) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);

        // Verificação centralizada de acesso (já verifica situação)
        accessControlService.verificarPermissao(usuario, ACEITAR_REVISAO_CADASTRO, sp);

        Unidade unidadeAnalise = sp.getUnidade().getUnidadeSuperior();
        if (unidadeAnalise == null) {
            throw new ErroInvarianteViolada(
                    "Unidade superior não encontrada para o subprocesso " + codSubprocesso);
        }

        Unidade unidadeDestino =
                unidadeAnalise.getUnidadeSuperior() != null
                        ? unidadeAnalise.getUnidadeSuperior()
                        : unidadeAnalise;

        workflowExecutor.registrarAnaliseETransicao(
                sp,
                REVISAO_CADASTRO_DISPONIBILIZADA,
                TipoTransicao.REVISAO_CADASTRO_ACEITA,
                TipoAnalise.CADASTRO,
                TipoAcaoAnalise.ACEITE_REVISAO,
                unidadeAnalise,
                unidadeAnalise, // Origin: Superior (who accepted)
                unidadeDestino, // Destination: Next Superior or same?
                usuario,
                observacoes,
                null
        );
    }

    @Transactional
    public void homologarRevisaoCadastro(Long codSubprocesso, String observacoes, Usuario usuario) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);

        // Verificação centralizada de acesso (já verifica situação)
        accessControlService.verificarPermissao(usuario, HOMOLOGAR_REVISAO_CADASTRO, sp);

        var impactos = impactoMapaService.verificarImpactos(codSubprocesso, usuario);

        if (impactos.isTemImpactos()) {
            Unidade sedoc = unidadeService.buscarEntidadePorSigla("SEDOC");

            sp.setSituacao(REVISAO_CADASTRO_HOMOLOGADA);
            repositorioSubprocesso.save(sp);

            transicaoService.registrar(
                    sp,
                    TipoTransicao.REVISAO_CADASTRO_HOMOLOGADA,
                    sedoc,
                    sedoc,
                    usuario,
                    observacoes);
        } else {
            sp.setSituacao(REVISAO_MAPA_HOMOLOGADO);
            repositorioSubprocesso.save(sp);
        }
    }

    private Subprocesso buscarSubprocesso(Long codSubprocesso) {
        return repositorioSubprocesso
                .findById(codSubprocesso)
                .orElseThrow(
                        () ->
                                new ErroEntidadeNaoEncontrada(
                                        "Subprocesso não encontrado: " + codSubprocesso));
    }

    @Transactional
    public void aceitarCadastroEmBloco(java.util.List<Long> unidadeCodigos, Long codSubprocessoBase, Usuario usuario) {
        unidadeCodigos.forEach(unidadeCodigo -> {
            // Lógica para encontrar o subprocesso de cada unidade no contexto do mesmo processo
            // Supondo que 'codSubprocessoBase' seja um dos subprocessos do processo ou que possamos buscar o subprocesso pelo processo e unidade.
            // Para simplificar, vou assumir que precisamos buscar o subprocesso da unidade dentro do mesmo processo do subprocesso base.

            Subprocesso base = buscarSubprocesso(codSubprocessoBase);
            Subprocesso target = repositorioSubprocesso.findByProcessoCodigoAndUnidadeCodigo(base.getProcesso().getCodigo(), unidadeCodigo)
                    .orElseThrow(() -> new sgc.comum.erros.ErroEntidadeDeveriaExistir(
                            "Subprocesso",
                            "processo=%d, unidade=%d".formatted(base.getProcesso().getCodigo(), unidadeCodigo),
                            "Workflow em bloco - subprocesso deveria ter sido criado no início do processo"));

            aceitarCadastro(target.getCodigo(), "De acordo com o cadastro de atividades da unidade (Em Bloco)", usuario);
        });
    }

    @Transactional
    public void homologarCadastroEmBloco(java.util.List<Long> unidadeCodigos, Long codSubprocessoBase, Usuario usuario) {
        unidadeCodigos.forEach(unidadeCodigo -> {
            Subprocesso base = buscarSubprocesso(codSubprocessoBase);
            Subprocesso target = repositorioSubprocesso.findByProcessoCodigoAndUnidadeCodigo(base.getProcesso().getCodigo(), unidadeCodigo)
                    .orElseThrow(() -> new sgc.comum.erros.ErroEntidadeDeveriaExistir(
                            "Subprocesso",
                            "processo=%d, unidade=%d".formatted(base.getProcesso().getCodigo(), unidadeCodigo),
                            "Workflow em bloco - subprocesso deveria ter sido criado no início do processo"));

            homologarCadastro(target.getCodigo(), "Homologação em bloco", usuario);
        });
    }
}
