package sgc.mapa.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import sgc.comum.erros.ErroAccessoNegado;
import sgc.organizacao.model.Usuario;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;

import java.util.List;
import java.util.Objects;

import static sgc.subprocesso.model.SituacaoSubprocesso.*;

/**
 * Serviço responsável por validar permissões de acesso e regras de negócio relacionadas
 * à situação do subprocesso para operações no mapa de competências.
 */
@Service
@Slf4j
public class MapaAcessoService {

    private static final String MSG_ERRO_CHEFE = """
            O chefe da unidade só pode verificar os impactos com o subprocesso na situação\
             'Revisão do cadastro em andamento'.""";
    private static final String MSG_ERRO_GESTOR = """
            O gestor só pode verificar os impactos com o subprocesso na situação 'Revisão do\
             cadastro disponibilizada'.""";
    private static final String MSG_ERRO_ADMIN = """
            O administrador só pode verificar os impactos com o subprocesso na situação 'Revisão\
             do cadastro disponibilizada', 'Revisão do cadastro homologada' ou 'Mapa\
             Ajustado'.""";

    /**
     * Verifica se o usuário tem permissão para realizar operações no mapa do subprocesso,
     * considerando seu perfil e a situação atual do subprocesso.
     *
     * @param usuario     O usuário autenticado.
     * @param subprocesso O subprocesso alvo da operação.
     * @throws ErroAccessoNegado se o usuário não tiver permissão.
     */
    public void verificarAcessoImpacto(Usuario usuario, Subprocesso subprocesso) {
        final SituacaoSubprocesso situacao = subprocesso.getSituacao();

        if (hasRole(usuario, "CHEFE")) {
            validarSituacao(situacao, List.of(REVISAO_CADASTRO_EM_ANDAMENTO, NAO_INICIADO), MSG_ERRO_CHEFE);
        } else if (hasRole(usuario, "GESTOR")) {
            validarSituacao(situacao, List.of(REVISAO_CADASTRO_DISPONIBILIZADA), MSG_ERRO_GESTOR);
        } else if (hasRole(usuario, "ADMIN")) {
            validarSituacao(
                    situacao,
                    List.of(
                            REVISAO_CADASTRO_DISPONIBILIZADA,
                            REVISAO_CADASTRO_HOMOLOGADA,
                            REVISAO_MAPA_AJUSTADO),
                    MSG_ERRO_ADMIN);
        }
    }

    private void validarSituacao(
            SituacaoSubprocesso atual, List<SituacaoSubprocesso> esperadas, String mensagemErro) {
        if (!esperadas.contains(atual)) throw new ErroAccessoNegado(mensagemErro);
    }

    private boolean hasRole(Usuario usuario, String role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() != null && auth.getPrincipal().equals(usuario)) {
            return auth.getAuthorities().stream()
                    .anyMatch(a -> Objects.equals(a.getAuthority(), "ROLE_%s".formatted(role)));
        }
        return usuario.getAuthorities().stream()
                .anyMatch(a -> Objects.equals(a.getAuthority(), "ROLE_%s".formatted(role)));
    }
}
