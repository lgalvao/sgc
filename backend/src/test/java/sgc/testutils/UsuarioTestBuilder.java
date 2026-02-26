package sgc.testutils;

import sgc.organizacao.model.*;

/**
 * Builder para criação de objetos Usuario em testes.
 * <p>
 * Implementa apenas os setters realmente presentes no modelo `Usuario`.
 */
public class UsuarioTestBuilder {
    private String tituloEleitoral = "999999";
    private String nome = "Usuário de Teste";
    private Unidade unidadeLotacao = null;

    public static UsuarioTestBuilder umDe() {
        return new UsuarioTestBuilder();
    }

    /**
     * Cria um usuário ADMIN padrão (191919)
     */
    public static UsuarioTestBuilder admin() {
        return new UsuarioTestBuilder()
            .comTitulo("191919")
            .comNome("Admin Teste");
    }

    /**
     * Cria um usuário GESTOR padrão (222222)
     */
    public static UsuarioTestBuilder gestor() {
        return new UsuarioTestBuilder()
            .comTitulo("222222")
            .comNome("Gestor Teste");
    }

    /**
     * Cria um usuário CHEFE padrão (555555)
     */
    public static UsuarioTestBuilder chefe() {
        return new UsuarioTestBuilder()
            .comTitulo("555555")
            .comNome("Chefe Teste");
    }

    /**
     * Cria um usuário SERVIDOR padrão (666666)
     */
    public static UsuarioTestBuilder servidor() {
        return new UsuarioTestBuilder()
            .comTitulo("666666")
            .comNome("Servidor Teste");
    }

    public UsuarioTestBuilder comTitulo(String tituloEleitoral) {
        this.tituloEleitoral = tituloEleitoral;
        return this;
    }

    public UsuarioTestBuilder comNome(String nome) {
        this.nome = nome;
        return this;
    }

    public UsuarioTestBuilder unidadeLotacao(Unidade unidade) {
        this.unidadeLotacao = unidade;
        return this;
    }

    public Usuario build() {
        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral(tituloEleitoral);
        usuario.setNome(nome);
        if (unidadeLotacao != null) {
            usuario.setUnidadeLotacao(unidadeLotacao);
        }
        // perfis/atribuições são representados por UsuarioPerfil; tests that need them
        // should use UsuarioFixture.adicionarPerfil(...) to attach UsuarioPerfil instances.
        return usuario;
    }
}
