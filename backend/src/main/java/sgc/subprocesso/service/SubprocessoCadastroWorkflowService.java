package sgc.subprocesso.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.analise.AnaliseService;
import sgc.analise.model.TipoAcaoAnalise;
import sgc.analise.model.TipoAnalise;
import sgc.comum.erros.ErroEntidadeDeveriaExistir;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.comum.erros.ErroInvarianteViolada;
import sgc.comum.erros.ErroValidacao;
import sgc.mapa.model.Mapa;
import sgc.mapa.service.ImpactoMapaService;
import sgc.organizacao.UnidadeService;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
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
        accessControlService.verificarPermissao(usuario, DISPONIBILIZAR_CADASTRO, sp);
        validarRequisitosNegocioParaDisponibilizacao(codSubprocesso, sp);
        
        Unidade origem = sp.getUnidade();
        Unidade destino = origem != null ? origem.getUnidadeSuperior() : null;
        
        sp.setSituacao(MAPEAMENTO_CADASTRO_DISPONIBILIZADO);
        sp.setDataFimEtapa1(java.time.LocalDateTime.now());
        repositorioSubprocesso.save(sp);

        analiseService.removerPorSubprocesso(sp.getCodigo());
        transicaoService.registrar(sp, TipoTransicao.CADASTRO_DISPONIBILIZADO, origem, destino, usuario);
    }

    @Transactional
    public void disponibilizarRevisao(Long codSubprocesso, Usuario usuario) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);
        accessControlService.verificarPermissao(usuario, DISPONIBILIZAR_REVISAO_CADASTRO, sp);
        validarRequisitosNegocioParaDisponibilizacao(codSubprocesso, sp);
        
        Unidade origem = sp.getUnidade();
        Unidade destino = origem != null ? origem.getUnidadeSuperior() : null;
        
        sp.setSituacao(REVISAO_CADASTRO_DISPONIBILIZADA);
        sp.setDataFimEtapa1(java.time.LocalDateTime.now());
        repositorioSubprocesso.save(sp);

        analiseService.removerPorSubprocesso(sp.getCodigo());
        transicaoService.registrar(sp, TipoTransicao.REVISAO_CADASTRO_DISPONIBILIZADA, origem, destino, usuario);
    }

    private void validarRequisitosNegocioParaDisponibilizacao(Long codSubprocesso, Subprocesso sp) {
        subprocessoService.validarExistenciaAtividades(codSubprocesso);

        if (!subprocessoService.obterAtividadesSemConhecimento(codSubprocesso).isEmpty()) {
            throw new ErroValidacao("Existem atividades sem conhecimentos associados.");
        }

        Mapa mapa = sp.getMapa();
        if (mapa == null || mapa.getCodigo() == null) {
            throw new ErroMapaNaoAssociado("Subprocesso sem mapa associado");
        }
    }

    @Transactional
    public void devolverCadastro(Long codSubprocesso, String observacoes, Usuario usuario) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);
        accessControlService.verificarPermissao(usuario, DEVOLVER_CADASTRO, sp);

        Unidade unidadeSubprocesso = sp.getUnidade();
        if (unidadeSubprocesso == null) {
            throw new ErroInvarianteViolada("Unidade não encontrada para o subprocesso " + codSubprocesso);
        }
        
        Unidade unidadeAnalise = unidadeSubprocesso.getUnidadeSuperior();
        if (unidadeAnalise == null) {
            throw new ErroInvarianteViolada("Unidade superior não encontrada para o subprocesso " + codSubprocesso);
        }

        sp.setDataFimEtapa1(null);
        transicaoService.registrarAnaliseETransicao(sp, MAPEAMENTO_CADASTRO_EM_ANDAMENTO, TipoTransicao.CADASTRO_DEVOLVIDO, TipoAnalise.CADASTRO, TipoAcaoAnalise.DEVOLUCAO_MAPEAMENTO, unidadeAnalise, unidadeAnalise, sp.getUnidade(), usuario, observacoes, null);
    }

    @Transactional
    public void aceitarCadastro(Long codSubprocesso, String observacoes, Usuario usuario) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);
        accessControlService.verificarPermissao(usuario, ACEITAR_CADASTRO, sp);

        Unidade unidadeOrigem = sp.getUnidade();
        Unidade unidadeDestino = unidadeOrigem.getUnidadeSuperior();
        if (unidadeDestino == null) {
            throw new ErroInvarianteViolada("Não foi possível identificar a unidade superior para enviar a análise.");
        }

        transicaoService.registrarAnaliseETransicao(sp, MAPEAMENTO_CADASTRO_DISPONIBILIZADO, TipoTransicao.CADASTRO_ACEITO, TipoAnalise.CADASTRO, TipoAcaoAnalise.ACEITE_MAPEAMENTO, unidadeDestino, unidadeOrigem, unidadeDestino, usuario, observacoes, null);
    }

    @Transactional
    public void homologarCadastro(Long codSubprocesso, String observacoes, Usuario usuario) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);
        accessControlService.verificarPermissao(usuario, HOMOLOGAR_CADASTRO, sp);

        Unidade sedoc = unidadeService.buscarEntidadePorSigla("SEDOC");
        sp.setSituacao(MAPEAMENTO_CADASTRO_HOMOLOGADO);
        repositorioSubprocesso.save(sp);

        transicaoService.registrar(sp, TipoTransicao.CADASTRO_HOMOLOGADO, sedoc, sedoc, usuario, observacoes);
    }

    @Transactional
    public void devolverRevisaoCadastro(Long codSubprocesso, String observacoes, Usuario usuario) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);
        accessControlService.verificarPermissao(usuario, DEVOLVER_REVISAO_CADASTRO, sp);

        Unidade unidadeSubprocesso = sp.getUnidade();
        if (unidadeSubprocesso == null) {
            throw new ErroInvarianteViolada("Unidade não encontrada para o subprocesso " + codSubprocesso);
        }

        Unidade unidadeAnalise = unidadeSubprocesso.getUnidadeSuperior();
        if (unidadeAnalise == null) {
            throw new ErroInvarianteViolada("Unidade superior não encontrada para o subprocesso " + codSubprocesso);
        }

        sp.setDataFimEtapa1(null);
        transicaoService.registrarAnaliseETransicao(sp, REVISAO_CADASTRO_EM_ANDAMENTO, TipoTransicao.REVISAO_CADASTRO_DEVOLVIDA, TipoAnalise.CADASTRO, TipoAcaoAnalise.DEVOLUCAO_REVISAO, unidadeAnalise, unidadeAnalise, sp.getUnidade(), usuario, observacoes, null);
    }

    @Transactional
    public void aceitarRevisaoCadastro(Long codSubprocesso, String observacoes, Usuario usuario) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);
        accessControlService.verificarPermissao(usuario, ACEITAR_REVISAO_CADASTRO, sp);

        Unidade unidadeSubprocesso = sp.getUnidade();
        if (unidadeSubprocesso == null) {
            throw new ErroInvarianteViolada("Unidade não encontrada para o subprocesso " + codSubprocesso);
        }
        
        Unidade unidadeAnalise = unidadeSubprocesso.getUnidadeSuperior();
        if (unidadeAnalise == null) {
            throw new ErroInvarianteViolada("Unidade superior não encontrada para o subprocesso " + codSubprocesso);
        }

        Unidade unidadeDestino = unidadeAnalise.getUnidadeSuperior() != null ? unidadeAnalise.getUnidadeSuperior() : unidadeAnalise;

        transicaoService.registrarAnaliseETransicao(sp, REVISAO_CADASTRO_DISPONIBILIZADA, TipoTransicao.REVISAO_CADASTRO_ACEITA, TipoAnalise.CADASTRO, TipoAcaoAnalise.ACEITE_REVISAO, unidadeAnalise, unidadeAnalise, unidadeDestino, usuario, observacoes, null);
    }

    @Transactional
    public void homologarRevisaoCadastro(Long codSubprocesso, String observacoes, Usuario usuario) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);
        accessControlService.verificarPermissao(usuario, HOMOLOGAR_REVISAO_CADASTRO, sp);

        var impactos = impactoMapaService.verificarImpactos(codSubprocesso, usuario);
        if (impactos.isTemImpactos()) {
            Unidade sedoc = unidadeService.buscarEntidadePorSigla("SEDOC");
            sp.setSituacao(REVISAO_CADASTRO_HOMOLOGADA);
            repositorioSubprocesso.save(sp);
            transicaoService.registrar(sp, TipoTransicao.REVISAO_CADASTRO_HOMOLOGADA, sedoc, sedoc, usuario, observacoes);
        } else {
            sp.setSituacao(REVISAO_MAPA_HOMOLOGADO);
            repositorioSubprocesso.save(sp);
        }
    }

    private Subprocesso buscarSubprocesso(Long codSubprocesso) {
        return repositorioSubprocesso
                .findById(codSubprocesso)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Subprocesso não encontrado: " + codSubprocesso));
    }

    @Transactional
    public void aceitarCadastroEmBloco(java.util.List<Long> unidadeCodigos, Long codSubprocessoBase, Usuario usuario) {
        unidadeCodigos.forEach(unidadeCodigo -> {
            Subprocesso base = buscarSubprocesso(codSubprocessoBase);
            Subprocesso target = repositorioSubprocesso.findByProcessoCodigoAndUnidadeCodigo(base.getProcesso().getCodigo(), unidadeCodigo)
                    .orElseThrow(() -> new ErroEntidadeDeveriaExistir("Subprocesso", "processo=%d, unidade=%d".formatted(base.getProcesso().getCodigo(), unidadeCodigo), "Workflow em bloco - subprocesso deveria ter sido criado no início do processo"));

            aceitarCadastro(target.getCodigo(), "De acordo com o cadastro de atividades da unidade (Em Bloco)", usuario);
        });
    }

    @Transactional
    public void homologarCadastroEmBloco(java.util.List<Long> unidadeCodigos, Long codSubprocessoBase, Usuario usuario) {
        unidadeCodigos.forEach(unidadeCodigo -> {
            Subprocesso base = buscarSubprocesso(codSubprocessoBase);
            Subprocesso target = repositorioSubprocesso.findByProcessoCodigoAndUnidadeCodigo(base.getProcesso().getCodigo(), unidadeCodigo)
                    .orElseThrow(() -> new ErroEntidadeDeveriaExistir(
                            "Subprocesso",
                            "processo=%d, unidade=%d".formatted(base.getProcesso().getCodigo(), unidadeCodigo),
                            "Workflow em bloco - subprocesso deveria ter sido criado no início do processo"));

            homologarCadastro(target.getCodigo(), "Homologação em bloco", usuario);
        });
    }
}
