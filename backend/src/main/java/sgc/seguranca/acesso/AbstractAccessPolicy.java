package sgc.seguranca.acesso;

import lombok.RequiredArgsConstructor;
import sgc.organizacao.model.Perfil;
import sgc.organizacao.model.Usuario;
import sgc.organizacao.model.UsuarioPerfil;
import sgc.organizacao.model.UsuarioPerfilRepo;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

/**
 * Classe base abstrata para políticas de acesso, centralizando lógica comum
 * de verificação de perfis e mensagens de erro.
 *
 * @param <T> O tipo do recurso protegido.
 */
@RequiredArgsConstructor
public abstract class AbstractAccessPolicy<T> implements AccessPolicy<T> {

    protected final UsuarioPerfilRepo usuarioPerfilRepo;
    protected String ultimoMotivoNegacao = "";

    @Override
    public String getMotivoNegacao() {
        return ultimoMotivoNegacao;
    }

    /**
     * Define o motivo da negação de forma padronizada.
     */
    protected void definirMotivoNegacao(Usuario usuario, EnumSet<Perfil> perfisPermitidos, Acao acao) {
        this.ultimoMotivoNegacao = String.format(
                "Usuário '%s' não possui um dos perfis necessários: %s. Ação: %s",
                usuario.getTituloEleitoral(),
                formatarPerfis(perfisPermitidos),
                acao.getDescricao()
        );
    }

    /**
     * Define um motivo de negação customizado.
     */
    protected void definirMotivoNegacao(String motivo) {
        this.ultimoMotivoNegacao = motivo;
    }

    protected boolean temPerfilPermitido(Usuario usuario, EnumSet<Perfil> perfisPermitidos) {
        Set<UsuarioPerfil> atribuicoes = new HashSet<>(
                usuarioPerfilRepo.findByUsuarioTitulo(usuario.getTituloEleitoral())
        );
        return usuario.getTodasAtribuicoes(atribuicoes).stream()
                .anyMatch(a -> perfisPermitidos.contains(a.getPerfil()));
    }

    protected String formatarPerfis(EnumSet<Perfil> perfis) {
        return perfis.stream()
                .map(Perfil::name)
                .reduce((a, b) -> a + ", " + b)
                .orElse("nenhum");
    }
}
