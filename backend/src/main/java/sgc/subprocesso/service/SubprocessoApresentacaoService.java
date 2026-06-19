package sgc.subprocesso.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.Mensagens;
import sgc.organizacao.UsuarioAplicacaoService;
import sgc.organizacao.service.UnidadeService;
import sgc.seguranca.AcaoPermissao;
import sgc.seguranca.SgcPermissionEvaluator;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.model.Analise;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.TipoAnalise;

@Service
@RequiredArgsConstructor
public class SubprocessoApresentacaoService {
    private final SubprocessoConsultaService consultaService;
    private final SubprocessoService subprocessoService;
    private final SubprocessoTransicaoService transicaoService;
    private final AnaliseHistoricoService analiseHistoricoService;
    private final UnidadeService unidadeService;
    private final UsuarioAplicacaoService usuarioAplicacaoService;
    private final SgcPermissionEvaluator permissionEvaluator;

    @Transactional(readOnly = true)
    public ContextoEdicaoResponse obterContextoEdicaoPorProcessoEUnidade(Long codProcesso, String siglaUnidade) {
        return consultaService.obterContextoEdicao(buscarSubprocessoComPermissaoVisualizacao(codProcesso, siglaUnidade));
    }

    @Transactional(readOnly = true)
    public ContextoCadastroAtividadesResponse obterContextoCadastroAtividadesPorProcessoEUnidade(Long codProcesso, String siglaUnidade) {
        return consultaService.obterContextoCadastroAtividades(buscarSubprocessoComPermissaoVisualizacao(codProcesso, siglaUnidade).getCodigo());
    }

    @Transactional
    public AtividadeOperacaoResponse importarAtividades(Long codSubprocesso, ImportarAtividadesRequest request) {
        SubprocessoService.ImportacaoAtividadesResultado resultado = subprocessoService.importarAtividades(
                codSubprocesso,
                request.codSubprocessoOrigem(),
                request.codigosAtividades()
        );
        Long codigoSubprocessoDestino = resultado.codigoSubprocessoDestino();
        return AtividadeOperacaoResponse.builder()
                .atividade(null)
                .subprocesso(consultaService.obterStatus(codigoSubprocessoDestino))
                .atividadesAtualizadas(consultaService.listarAtividadesSubprocesso(codigoSubprocessoDestino))
                .permissoes(consultaService.obterPermissoesUI(codigoSubprocessoDestino))
                .message("Atividades importadas.")
                .aviso(resultado.temDuplicatas() ? Mensagens.IMPORTACAO_ATIVIDADES_DUPLICADAS : null)
                .build();
    }

    @Transactional(readOnly = true)
    public AnaliseHistoricoDto criarAnalise(Long codSubprocesso, CriarAnaliseRequest request, TipoAnalise tipo) {
        Subprocesso subprocesso = consultaService.buscarSubprocesso(codSubprocesso);
        Analise analise = transicaoService.criarAnalise(subprocesso, request, tipo);
        return analiseHistoricoService.converter(analise);
    }

    private Subprocesso buscarSubprocessoComPermissaoVisualizacao(Long codProcesso, String siglaUnidade) {
        Long codUnidade = unidadeService.buscarCodigoPorSigla(siglaUnidade);
        Subprocesso subprocesso = consultaService.obterEntidadePorProcessoEUnidade(codProcesso, codUnidade);
        if (!permissionEvaluator.verificarPermissao(
                usuarioAplicacaoService.usuarioAutenticado(),
                subprocesso,
                AcaoPermissao.VISUALIZAR_SUBPROCESSO
        )) {
            throw new AccessDeniedException("Acesso negado ao subprocesso");
        }
        return subprocesso;
    }
}
