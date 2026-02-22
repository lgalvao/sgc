package sgc.seguranca;

import lombok.RequiredArgsConstructor;
import sgc.organizacao.model.Perfil;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.organizacao.model.UsuarioPerfilRepo;
import sgc.organizacao.service.HierarquiaService;

import java.util.EnumSet;
import java.util.Objects;

/**
 * Classe base abstrata para políticas de acesso, centralizando lógica comum
 * de verificação de perfis, hierarquia e mensagens de erro.
 *
 * @param <T> O tipo do recurso protegido.
 */
@RequiredArgsConstructor
public abstract class AbstractAccessPolicy<T> implements AccessPolicy<T> {

    protected final UsuarioPerfilRepo usuarioPerfilRepo;
    protected final HierarquiaService hierarquiaService;

    private String motivoNegacao;

    @Override
    public String getMotivoNegacao() {
        return motivoNegacao;
    }

    protected void definirMotivoNegacao(String motivo) {
        this.motivoNegacao = motivo;
    }

    protected void definirMotivoNegacao(Usuario usuario, EnumSet<Perfil> perfisPermitidos, Acao acao) {
        this.motivoNegacao = String.format("Usuário '%s' com perfil '%s' não tem permissão para a ação '%s'. Perfis permitidos: %s",
                usuario.getTituloEleitoral(),
                usuario.getPerfilAtivo(),
                acao.getDescricao(),
                perfisPermitidos);
    }

    protected boolean temPerfilPermitido(Usuario usuario, EnumSet<Perfil> perfisPermitidos) {
        return perfisPermitidos.contains(usuario.getPerfilAtivo());
    }

    protected boolean estaNaUnidade(Usuario usuario, Unidade unidade) {
        return Objects.equals(usuario.getUnidadeAtivaCodigo(), unidade.getCodigo());
    }

    protected boolean estaNaMesmaOuSubordinada(Usuario usuario, Unidade unidade) {
        return hierarquiaService.isMesmaOuSubordinada(unidade, Unidade.builder().codigo(usuario.getUnidadeAtivaCodigo()).build());
    }

    public enum RequisitoHierarquia {
        NENHUM, MESMA_UNIDADE, MESMA_OU_SUBORDINADA, SUPERIOR_IMEDIATA, TITULAR_UNIDADE
    }

    protected boolean verificaHierarquia(Usuario usuario, Unidade unidade, RequisitoHierarquia requisito) {
        if (usuario.getPerfilAtivo() == Perfil.ADMIN && requisito != RequisitoHierarquia.TITULAR_UNIDADE && requisito != RequisitoHierarquia.MESMA_UNIDADE) {
            return true;
        }

        return switch (requisito) {
            case NENHUM -> true;
            case MESMA_UNIDADE -> Objects.equals(usuario.getUnidadeAtivaCodigo(), unidade.getCodigo());
            case MESMA_OU_SUBORDINADA -> hierarquiaService.isMesmaOuSubordinada(unidade, Unidade.builder().codigo(usuario.getUnidadeAtivaCodigo()).build());
            case SUPERIOR_IMEDIATA -> hierarquiaService.isSuperiorImediata(unidade, Unidade.builder().codigo(usuario.getUnidadeAtivaCodigo()).build());
            case TITULAR_UNIDADE -> hierarquiaService.isResponsavel(unidade, usuario);
        };
    }

    protected String obterMotivoNegacaoHierarquia(Usuario usuario, Unidade unidade, RequisitoHierarquia requisito) {
        return switch (requisito) {
            case MESMA_UNIDADE -> String.format("Usuário está na unidade '%s', mas a ação exige a unidade '%s'",
                    usuario.getUnidadeAtivaCodigo(), unidade.getSigla());
            case MESMA_OU_SUBORDINADA -> String.format("Unidade '%s' não é subordinada à unidade do usuário", unidade.getSigla());
            case SUPERIOR_IMEDIATA -> String.format("Unidade '%s' não é subordinada direta à unidade do usuário", unidade.getSigla());
            case TITULAR_UNIDADE -> String.format("Usuário '%s' não é o responsável pela unidade '%s'",
                    usuario.getTituloEleitoral(), unidade.getSigla());
            default -> "Erro inesperado na verificação de hierarquia";
        };
    }

    protected String formatarPerfis(EnumSet<Perfil> perfis) {
        if (perfis == null || perfis.isEmpty()) {
            return "nenhum";
        }
        return perfis.toString();
    }
}
