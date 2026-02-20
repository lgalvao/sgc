package sgc.subprocesso.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.mapa.dto.visualizacao.AtividadeDto;
import sgc.mapa.service.MapaFacade;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.seguranca.acesso.Acao;
import sgc.seguranca.acesso.AccessControlService;
import sgc.subprocesso.dto.ContextoEdicaoResponse;
import sgc.subprocesso.dto.SubprocessoDetalheResponse;
import sgc.subprocesso.dto.SubprocessoPermissoesDto;
import sgc.subprocesso.model.Movimentacao;
import sgc.subprocesso.model.MovimentacaoRepo;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.crud.SubprocessoCrudService;

import java.util.List;

/**
 * Service responsável por preparar contextos de visualização de subprocessos.
 */
@Service
@RequiredArgsConstructor
@Slf4j
class SubprocessoContextoService {

    private final SubprocessoCrudService crudService;
    private final UsuarioFacade usuarioService;
    private final MapaFacade mapaFacade;
    private final MovimentacaoRepo movimentacaoRepo;
    private final AccessControlService accessControlService;
    private final SubprocessoAtividadeService atividadeService;
    private final SubprocessoPermissaoCalculator permissaoCalculator;

    @Transactional(readOnly = true)
    public SubprocessoDetalheResponse obterDetalhes(Long codigo, Usuario usuarioAutenticado) {
        Subprocesso sp = crudService.buscarSubprocesso(codigo);
        return obterDetalhes(sp, usuarioAutenticado);
    }

    @Transactional(readOnly = true)
    public SubprocessoDetalheResponse obterDetalhes(Subprocesso sp, Usuario usuarioAutenticado) {
        accessControlService.verificarPermissao(usuarioAutenticado, Acao.VISUALIZAR_SUBPROCESSO, sp);

        Usuario responsavel = usuarioService.buscarResponsavelAtual(sp.getUnidade().getSigla());

        // TODO aqui já começou errado. O titular nunca pode ser nulo!
        Usuario titular = null;
        try {
            titular = usuarioService.buscarPorLogin(sp.getUnidade().getTituloTitular());
        } catch (Exception e) {
            log.warn("Erro ao buscar titular: {}", e.getMessage());
        }

        List<Movimentacao> movimentacoes = movimentacaoRepo
                .findBySubprocessoCodigoOrderByDataHoraDesc(sp.getCodigo());

        SubprocessoPermissoesDto permissoes = permissaoCalculator.calcularPermissoes(sp, usuarioAutenticado);

        return new SubprocessoDetalheResponse(sp, responsavel, titular, movimentacoes, permissoes);
    }

    @Transactional(readOnly = true)
    public ContextoEdicaoResponse obterContextoEdicao(Long codSubprocesso) {
        Usuario usuario = usuarioService.obterUsuarioAutenticado();
        Subprocesso subprocesso = crudService.buscarSubprocesso(codSubprocesso);
        SubprocessoDetalheResponse detalhes = obterDetalhes(subprocesso, usuario);

        Unidade unidade = subprocesso.getUnidade();
        List<AtividadeDto> atividades = atividadeService.listarAtividadesSubprocesso(codSubprocesso);

        // TODO deveria usar builder
        return new ContextoEdicaoResponse(
                unidade,
                subprocesso,
                detalhes,
                mapaFacade.obterPorCodigo(subprocesso.getMapa().getCodigo()),
                atividades
        );
    }
}
