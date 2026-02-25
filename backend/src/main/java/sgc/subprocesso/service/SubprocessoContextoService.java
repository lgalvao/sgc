package sgc.subprocesso.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.mapa.MapaFacade;
import sgc.mapa.dto.AtividadeDto;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.processo.model.SituacaoProcesso;
import sgc.seguranca.SgcPermissionEvaluator;
import sgc.subprocesso.dto.ContextoEdicaoResponse;
import sgc.subprocesso.dto.PermissoesSubprocessoDto;
import sgc.subprocesso.dto.SubprocessoDetalheResponse;
import sgc.subprocesso.model.*;

import java.util.List;
import java.util.Set;

import static sgc.subprocesso.model.SituacaoSubprocesso.*;

/**
 * Prepara contextos de visualização de subprocessos.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
class SubprocessoContextoService {
    private final SubprocessoRepo subprocessoRepo;
    private final UsuarioFacade usuarioService;
    private final MapaFacade mapaFacade;
    private final MovimentacaoRepo movimentacaoRepo;
    private final SubprocessoAtividadeService atividadeService;
    private final SgcPermissionEvaluator permissionEvaluator;

    public SubprocessoDetalheResponse obterDetalhes(Long codigo, Usuario usuarioAutenticado) {
        Subprocesso sp = subprocessoRepo.findByIdWithMapaAndAtividades(codigo).orElseThrow(() -> new ErroEntidadeNaoEncontrada("Subprocesso", codigo));
        return obterDetalhes(sp, usuarioAutenticado);
    }

    public SubprocessoDetalheResponse obterDetalhes(Subprocesso sp, Usuario usuarioAutenticado) {
        String siglaUnidade = sp.getUnidade().getSigla();
        String localizacaoAtual = siglaUnidade;

        Usuario responsavel = usuarioService.buscarResponsavelAtual(siglaUnidade);
        Usuario titular = null;
        try {
            titular = usuarioService.buscarPorLogin(sp.getUnidade().getTituloTitular());
        } catch (Exception e) {
            log.warn("Erro ao buscar titular da unidade {}: {}", siglaUnidade, e.getMessage());
        }

        List<Movimentacao> movimentacoes = movimentacaoRepo.findBySubprocessoCodigoOrderByDataHoraDesc(sp.getCodigo());
        if (!movimentacoes.isEmpty()) {
            Unidade destino = movimentacoes.getFirst().getUnidadeDestino();
            if (destino != null) {
                localizacaoAtual = destino.getSigla();
            }
        }

        PermissoesSubprocessoDto permissoes = obterPermissoesUI(sp, usuarioAutenticado);

        return SubprocessoDetalheResponse.builder()
                .subprocesso(sp)
                .responsavel(responsavel)
                .titular(titular)
                .movimentacoes(movimentacoes)
                .localizacaoAtual(localizacaoAtual)
                .permissoes(permissoes)
                .build();
    }

    public ContextoEdicaoResponse obterContextoEdicao(Long codSubprocesso) {
        Usuario usuario = usuarioService.usuarioAutenticado();
        Subprocesso subprocesso = subprocessoRepo.findByIdWithMapaAndAtividades(codSubprocesso).orElseThrow(() -> new ErroEntidadeNaoEncontrada("Subprocesso", codSubprocesso));
        SubprocessoDetalheResponse detalhes = obterDetalhes(subprocesso, usuario);

        Unidade unidade = subprocesso.getUnidade();
        List<AtividadeDto> atividades = atividadeService.listarAtividadesSubprocesso(codSubprocesso);

        return new ContextoEdicaoResponse(
                unidade,
                subprocesso,
                detalhes,
                mapaFacade.mapaPorCodigo(subprocesso.getMapa().getCodigo()),
                atividades
        );
    }

    public PermissoesSubprocessoDto obterPermissoesUI(Subprocesso sp, Usuario usuario) {
        return PermissoesSubprocessoDto.builder()
            .podeEditarCadastro(
                canExecute(usuario, sp, "EDITAR_CADASTRO", Set.of(NAO_INICIADO, MAPEAMENTO_CADASTRO_EM_ANDAMENTO)) ||
                canExecute(usuario, sp, "EDITAR_REVISAO_CADASTRO", Set.of(NAO_INICIADO, REVISAO_CADASTRO_EM_ANDAMENTO)))
            .podeDisponibilizarCadastro(
                canExecute(usuario, sp, "DISPONIBILIZAR_CADASTRO", Set.of(MAPEAMENTO_CADASTRO_EM_ANDAMENTO)) ||
                canExecute(usuario, sp, "DISPONIBILIZAR_REVISAO_CADASTRO", Set.of(REVISAO_CADASTRO_EM_ANDAMENTO)))
            .podeDevolverCadastro(
                canExecute(usuario, sp, "DEVOLVER_CADASTRO", Set.of(MAPEAMENTO_CADASTRO_DISPONIBILIZADO)) ||
                canExecute(usuario, sp, "DEVOLVER_REVISAO_CADASTRO", Set.of(REVISAO_CADASTRO_DISPONIBILIZADA)))
            .podeAceitarCadastro(
                canExecute(usuario, sp, "ACEITAR_CADASTRO", Set.of(MAPEAMENTO_CADASTRO_DISPONIBILIZADO)) ||
                canExecute(usuario, sp, "ACEITAR_REVISAO_CADASTRO", Set.of(REVISAO_CADASTRO_DISPONIBILIZADA)))
            .podeHomologarCadastro(
                canExecute(usuario, sp, "HOMOLOGAR_CADASTRO", Set.of(MAPEAMENTO_CADASTRO_DISPONIBILIZADO)) ||
                canExecute(usuario, sp, "HOMOLOGAR_REVISAO_CADASTRO", Set.of(REVISAO_CADASTRO_DISPONIBILIZADA)))
            .podeEditarMapa(
                canExecute(usuario, sp, "EDITAR_MAPA", Set.of(
                    NAO_INICIADO, MAPEAMENTO_CADASTRO_EM_ANDAMENTO, MAPEAMENTO_CADASTRO_HOMOLOGADO,
                    MAPEAMENTO_MAPA_CRIADO, MAPEAMENTO_MAPA_COM_SUGESTOES,
                    REVISAO_CADASTRO_EM_ANDAMENTO, REVISAO_CADASTRO_HOMOLOGADA,
                    REVISAO_MAPA_AJUSTADO, REVISAO_MAPA_COM_SUGESTOES,
                    DIAGNOSTICO_AUTOAVALIACAO_EM_ANDAMENTO)))
            .podeDisponibilizarMapa(
                canExecute(usuario, sp, "DISPONIBILIZAR_MAPA", Set.of(
                    MAPEAMENTO_CADASTRO_HOMOLOGADO, MAPEAMENTO_MAPA_CRIADO, MAPEAMENTO_MAPA_COM_SUGESTOES,
                    REVISAO_CADASTRO_HOMOLOGADA, REVISAO_MAPA_AJUSTADO, REVISAO_MAPA_COM_SUGESTOES)))
            .podeValidarMapa(
                canExecute(usuario, sp, "VALIDAR_MAPA", Set.of(MAPEAMENTO_MAPA_DISPONIBILIZADO, REVISAO_MAPA_DISPONIBILIZADO)))
            .podeApresentarSugestoes(
                canExecute(usuario, sp, "APRESENTAR_SUGESTOES", Set.of(MAPEAMENTO_MAPA_DISPONIBILIZADO, REVISAO_MAPA_DISPONIBILIZADO)))
            .podeDevolverMapa(
                canExecute(usuario, sp, "DEVOLVER_MAPA", Set.of(
                    MAPEAMENTO_MAPA_COM_SUGESTOES, MAPEAMENTO_MAPA_VALIDADO,
                    REVISAO_MAPA_COM_SUGESTOES, REVISAO_MAPA_VALIDADO)))
            .podeAceitarMapa(
                canExecute(usuario, sp, "ACEITAR_MAPA", Set.of(
                    MAPEAMENTO_MAPA_COM_SUGESTOES, MAPEAMENTO_MAPA_VALIDADO,
                    REVISAO_MAPA_COM_SUGESTOES, REVISAO_MAPA_VALIDADO)))
            .podeHomologarMapa(
                canExecute(usuario, sp, "HOMOLOGAR_MAPA", Set.of(
                    MAPEAMENTO_MAPA_COM_SUGESTOES, MAPEAMENTO_MAPA_VALIDADO,
                    REVISAO_MAPA_COM_SUGESTOES, REVISAO_MAPA_VALIDADO)))
            .podeVisualizarImpacto(
                canExecute(usuario, sp, "VERIFICAR_IMPACTOS", Set.of(
                    NAO_INICIADO, REVISAO_CADASTRO_EM_ANDAMENTO,
                    REVISAO_CADASTRO_DISPONIBILIZADA, REVISAO_CADASTRO_HOMOLOGADA,
                    REVISAO_MAPA_AJUSTADO)))
            .podeAlterarDataLimite(
                canExecute(usuario, sp, "ALTERAR_DATA_LIMITE", Set.of(SituacaoSubprocesso.values())))
            .podeReabrirCadastro(
                canExecute(usuario, sp, "REABRIR_CADASTRO", Set.of(SituacaoSubprocesso.values())))
            .podeReabrirRevisao(
                canExecute(usuario, sp, "REABRIR_REVISAO", Set.of(SituacaoSubprocesso.values())))
            .podeEnviarLembrete(
                canExecute(usuario, sp, "ENVIAR_LEMBRETE_PROCESSO", Set.of(SituacaoSubprocesso.values())))
            .build();
    }

    private boolean canExecute(Usuario usuario, Subprocesso sp, String acao, Set<SituacaoSubprocesso> allowedStates) {
        if (sp.getProcesso() != null && sp.getProcesso().getSituacao() == SituacaoProcesso.FINALIZADO) {
            return false;
        }
        if (!allowedStates.contains(sp.getSituacao())) return false;
        return permissionEvaluator.checkPermission(usuario, sp, acao);
    }
}
