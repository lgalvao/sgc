package sgc.subprocesso.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sgc.atividade.model.AtividadeRepo;
import sgc.comum.erros.ErroAccessoNegado;
import sgc.sgrh.model.Perfil;
import sgc.sgrh.model.Usuario;
import sgc.subprocesso.dto.SubprocessoPermissoesDto;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.unidade.model.Unidade;

import java.util.EnumSet;
import java.util.Objects;

import static sgc.processo.model.TipoProcesso.REVISAO;
import static sgc.subprocesso.model.SituacaoSubprocesso.*;

@Service
@RequiredArgsConstructor
public class SubprocessoPermissoesService {
    private static final String ACAO_ENVIAR_REVISAO = "ENVIAR_REVISAO";
    private static final String ACAO_AJUSTAR_MAPA = "AJUSTAR_MAPA";

    private final AtividadeRepo atividadeRepo;

    public void validar(Subprocesso subprocesso, Long unidadeCodigo, String acao) {
        if (!subprocesso.getUnidade().getCodigo().equals(unidadeCodigo)) {
            // TODO explicar melhor na mensagem o contexto do erro
            throw new ErroAccessoNegado("Unidade sem acesso a este subprocesso.");
        }

        SituacaoSubprocesso situacaoSubprocesso = subprocesso.getSituacao();

        if (ACAO_ENVIAR_REVISAO.equals(acao)) {
            if (REVISAO_CADASTRO_EM_ANDAMENTO != situacaoSubprocesso) {
                // TODO explicar melhor na mensagem o contexto do erro
                throw new ErroAccessoNegado("Situação inválida.");
            }

        } else if (ACAO_AJUSTAR_MAPA.equals(acao)) {
            if (REVISAO_CADASTRO_HOMOLOGADA != situacaoSubprocesso
                    && REVISAO_MAPA_AJUSTADO != situacaoSubprocesso) {
                // TODO explicar melhor na mensagem o contexto do erro
                throw new ErroAccessoNegado("Situação inválida para ajuste.");
            }

            if (subprocesso.getMapa() != null
                    && atividadeRepo.countByMapaCodigo(subprocesso.getMapa().getCodigo()) == 0
                    && REVISAO_MAPA_AJUSTADO == situacaoSubprocesso) {
                // TODO explicar melhor na mensagem o contexto do erro
                throw new ErroAccessoNegado("Mapa vazio.");
            }
        }
    }

    public SubprocessoPermissoesDto calcularPermissoes(Subprocesso sp, Usuario usuario) {
        boolean isAdmin = usuario.getTodasAtribuicoes().stream()
                .anyMatch(a -> a.getPerfil() == Perfil.ADMIN);

        Long spUnidadeCodigo = sp.getUnidade() != null ? sp.getUnidade().getCodigo() : null;

        boolean isGestorUnidade =
                spUnidadeCodigo != null
                        && usuario.getTodasAtribuicoes().stream()
                        .anyMatch(a -> a.getPerfil() == Perfil.GESTOR
                                && a.getUnidade() != null
                                && a.getUnidade().getCodigo() != null
                                && (a.getUnidade().getCodigo().equals(spUnidadeCodigo)
                                || isSubordinada(
                                sp.getUnidade(),
                                a.getUnidade()))
                        );

        boolean isChefeOuServidorUnidade =
                spUnidadeCodigo != null
                        && usuario.getTodasAtribuicoes().stream()
                        .anyMatch(a -> (a.getPerfil() == Perfil.CHEFE
                                || a.getPerfil() == Perfil.SERVIDOR)
                                && a.getUnidade() != null
                                && a.getUnidade().getCodigo() != null
                                && a.getUnidade().getCodigo().equals(spUnidadeCodigo)
                        );

        boolean acessoEdicao = isAdmin || isGestorUnidade || isChefeOuServidorUnidade;

        boolean isRevisao = sp.getProcesso().getTipo() == REVISAO;

        boolean situacaoImpactoValida =
                (isRevisao && sp.getSituacao() == NAO_INICIADO)
                        || sp.getSituacao() == MAPEAMENTO_CADASTRO_HOMOLOGADO
                        || sp.getSituacao() == REVISAO_CADASTRO_EM_ANDAMENTO
                        || sp.getSituacao() == SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA
                        || sp.getSituacao() == REVISAO_CADASTRO_HOMOLOGADA
                        || sp.getSituacao() == REVISAO_MAPA_AJUSTADO;

        boolean podeVisualizarImpacto;
        if (acessoEdicao) {
            podeVisualizarImpacto = situacaoImpactoValida;
        } else {
            podeVisualizarImpacto = false;
        }

        EnumSet<SituacaoSubprocesso> situacoesPermitemEdicao = EnumSet.of(
                NAO_INICIADO,
                MAPEAMENTO_CADASTRO_EM_ANDAMENTO,
                MAPEAMENTO_CADASTRO_HOMOLOGADO,
                MAPEAMENTO_MAPA_CRIADO,
                MAPEAMENTO_MAPA_COM_SUGESTOES,
                REVISAO_CADASTRO_EM_ANDAMENTO,
                REVISAO_CADASTRO_HOMOLOGADA, // CDU-16: ADMIN ajusta mapa neste estado
                REVISAO_MAPA_AJUSTADO,
                REVISAO_MAPA_COM_SUGESTOES,
                DIAGNOSTICO_AUTOAVALIACAO_EM_ANDAMENTO);

        boolean situacaoPermiteEdicao = situacoesPermitemEdicao.contains(sp.getSituacao());

        boolean podeEditarMapa = acessoEdicao && situacaoPermiteEdicao;

        return SubprocessoPermissoesDto.builder()
                .podeEditarMapa(podeEditarMapa)
                .podeVisualizarMapa(true)
                .podeVerPagina(true)
                .podeDisponibilizarCadastro(podeEditarMapa)
                .podeDevolverCadastro(isAdmin || isGestorUnidade)
                .podeAceitarCadastro(isAdmin || isGestorUnidade)
                .podeVisualizarDiagnostico(true)
                .podeRealizarAutoavaliacao(acessoEdicao && DIAGNOSTICO_AUTOAVALIACAO_EM_ANDAMENTO == sp.getSituacao())
                .podeAlterarDataLimite(isAdmin)
                .podeVisualizarImpacto(podeVisualizarImpacto)
                .build();
    }

    private boolean isSubordinada(Unidade alvo, Unidade superior) {
        if (alvo == null || superior == null || alvo.getUnidadeSuperior() == null) return false;

        Unidade atual = alvo;
        while (atual != null) {
            if (Objects.equals(superior.getCodigo(), atual.getCodigo())) return true;
            atual = atual.getUnidadeSuperior();
        }
        return false;
    }
}
