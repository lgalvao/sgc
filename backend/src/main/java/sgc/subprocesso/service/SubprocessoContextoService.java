package sgc.subprocesso.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.mapa.MapaFacade;
import sgc.mapa.dto.AtividadeDto;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.model.Perfil;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.processo.model.SituacaoProcesso;
import sgc.subprocesso.dto.ContextoEdicaoResponse;
import sgc.subprocesso.dto.PermissoesSubprocessoDto;
import sgc.subprocesso.dto.SubprocessoDetalheResponse;
import sgc.subprocesso.model.*;

import java.util.List;
import java.util.Objects;
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
        if (sp.getProcesso() != null && sp.getProcesso().getSituacao() == SituacaoProcesso.FINALIZADO) {
            return PermissoesSubprocessoDto.builder().build(); // Tudo false
        }

        Unidade localizacao = obterUnidadeLocalizacao(sp);
        boolean isNaUnidade = Objects.equals(usuario.getUnidadeAtivaCodigo(), localizacao.getCodigo());
        Perfil perfil = usuario.getPerfilAtivo();
        SituacaoSubprocesso situacao = sp.getSituacao();

        boolean isChefe = perfil == Perfil.CHEFE;
        boolean isGestor = perfil == Perfil.GESTOR;
        boolean isAdmin = perfil == Perfil.ADMIN;

        return PermissoesSubprocessoDto.builder()
            .podeEditarCadastro(isNaUnidade && isChefe && Set.of(NAO_INICIADO, MAPEAMENTO_CADASTRO_EM_ANDAMENTO, REVISAO_CADASTRO_EM_ANDAMENTO).contains(situacao))
            .podeDisponibilizarCadastro(isNaUnidade && isChefe && Set.of(MAPEAMENTO_CADASTRO_EM_ANDAMENTO, REVISAO_CADASTRO_EM_ANDAMENTO).contains(situacao))
            .podeDevolverCadastro(isNaUnidade && (isGestor || isAdmin) && Set.of(MAPEAMENTO_CADASTRO_DISPONIBILIZADO, REVISAO_CADASTRO_DISPONIBILIZADA).contains(situacao))
            .podeAceitarCadastro(isNaUnidade && isGestor && Set.of(MAPEAMENTO_CADASTRO_DISPONIBILIZADO, REVISAO_CADASTRO_DISPONIBILIZADA).contains(situacao))
            .podeHomologarCadastro(isNaUnidade && isAdmin && Set.of(MAPEAMENTO_CADASTRO_DISPONIBILIZADO, REVISAO_CADASTRO_DISPONIBILIZADA).contains(situacao))
            .podeEditarMapa(isNaUnidade && isAdmin && Set.of(
                    NAO_INICIADO, MAPEAMENTO_CADASTRO_EM_ANDAMENTO, MAPEAMENTO_CADASTRO_HOMOLOGADO,
                    MAPEAMENTO_MAPA_CRIADO, MAPEAMENTO_MAPA_COM_SUGESTOES,
                    REVISAO_CADASTRO_EM_ANDAMENTO, REVISAO_CADASTRO_HOMOLOGADA,
                    REVISAO_MAPA_AJUSTADO, REVISAO_MAPA_COM_SUGESTOES,
                    DIAGNOSTICO_AUTOAVALIACAO_EM_ANDAMENTO).contains(situacao))
            .podeDisponibilizarMapa(isNaUnidade && isAdmin && Set.of(
                    MAPEAMENTO_CADASTRO_HOMOLOGADO, MAPEAMENTO_MAPA_CRIADO, MAPEAMENTO_MAPA_COM_SUGESTOES,
                    REVISAO_CADASTRO_HOMOLOGADA, REVISAO_MAPA_AJUSTADO, REVISAO_MAPA_COM_SUGESTOES).contains(situacao))
            .podeValidarMapa(isNaUnidade && isChefe && Set.of(MAPEAMENTO_MAPA_DISPONIBILIZADO, REVISAO_MAPA_DISPONIBILIZADO).contains(situacao))
            .podeApresentarSugestoes(isNaUnidade && isChefe && Set.of(MAPEAMENTO_MAPA_DISPONIBILIZADO, REVISAO_MAPA_DISPONIBILIZADO).contains(situacao))
            .podeDevolverMapa(isNaUnidade && (isGestor || isAdmin) && Set.of(
                    MAPEAMENTO_MAPA_COM_SUGESTOES, MAPEAMENTO_MAPA_VALIDADO,
                    REVISAO_MAPA_COM_SUGESTOES, REVISAO_MAPA_VALIDADO).contains(situacao))
            .podeAceitarMapa(isNaUnidade && isGestor && Set.of(
                    MAPEAMENTO_MAPA_COM_SUGESTOES, MAPEAMENTO_MAPA_VALIDADO,
                    REVISAO_MAPA_COM_SUGESTOES, REVISAO_MAPA_VALIDADO).contains(situacao))
            .podeHomologarMapa(isNaUnidade && isAdmin && Set.of(
                    MAPEAMENTO_MAPA_COM_SUGESTOES, MAPEAMENTO_MAPA_VALIDADO,
                    REVISAO_MAPA_COM_SUGESTOES, REVISAO_MAPA_VALIDADO).contains(situacao))
            .podeVisualizarImpacto(isAdmin || (isNaUnidade && (isChefe || isGestor) && Set.of(
                    NAO_INICIADO, REVISAO_CADASTRO_EM_ANDAMENTO,
                    REVISAO_CADASTRO_DISPONIBILIZADA, REVISAO_CADASTRO_HOMOLOGADA,
                    REVISAO_MAPA_AJUSTADO).contains(situacao)))
            .podeAlterarDataLimite(isAdmin)
            .podeReabrirCadastro(isAdmin)
            .podeReabrirRevisao(isAdmin)
            .podeEnviarLembrete(isAdmin || isGestor)
            .build();
    }

    private Unidade obterUnidadeLocalizacao(Subprocesso sp) {
        if (sp.getLocalizacaoAtualCache() != null) return sp.getLocalizacaoAtualCache();
        if (sp.getCodigo() == null) return sp.getUnidade();
        return movimentacaoRepo.findFirstBySubprocessoCodigoOrderByDataHoraDesc(sp.getCodigo())
                .filter(m -> m.getUnidadeDestino() != null)
                .map(Movimentacao::getUnidadeDestino)
                .orElse(sp.getUnidade());
    }
}
