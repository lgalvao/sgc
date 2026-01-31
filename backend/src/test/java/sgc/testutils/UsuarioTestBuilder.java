package sgc.testutils;

import sgc.organizacao.model.Perfil;
import sgc.organizacao.model.Usuario;

import java.util.HashSet;
import java.util.Set;

/**
 * Builder para criação de objetos Usuario em testes.
 * 
 * Elimina a necessidade de mocks complexos e setup repetitivo.
 * 
 * @example
 * ```java
 * // Ao invés de:
 * Usuario usuario = mock(Usuario.class);
 * when(usuario.getTitulo()).thenReturn("191919");
 * when(usuario.getNome()).thenReturn("Admin Teste");
 * when(usuario.getCodigoUnidade()).thenReturn("SEDOC");
 * Set<Perfil> perfis = new HashSet<>();
 * perfis.add(Perfil.ADMIN);
 * when(usuario.getPerfis()).thenReturn(perfis);
 * 
 * // Use:
 * Usuario usuario = UsuarioTestBuilder.admin().build();
 * ```
 */
public class UsuarioTestBuilder {
    private String titulo = "999999";
    private String nome = "Usuário de Teste";
    private String codigoUnidade = "SEDOC";
    private final Set<Perfil> perfis = new HashSet<>();

    public static UsuarioTestBuilder umDe() {
        return new UsuarioTestBuilder();
    }

    /**
     * Cria um usuário ADMIN padrão (191919)
     */
    public static UsuarioTestBuilder admin() {
        return new UsuarioTestBuilder()
            .comTitulo("191919")
            .comNome("Admin Teste")
            .comUnidade("SEDOC")
            .comPerfil(Perfil.ADMIN);
    }

    /**
     * Cria um usuário GESTOR padrão (222222)
     */
    public static UsuarioTestBuilder gestor() {
        return new UsuarioTestBuilder()
            .comTitulo("222222")
            .comNome("Gestor Teste")
            .comUnidade("COORD_11")
            .comPerfil(Perfil.GESTOR);
    }

    /**
     * Cria um usuário CHEFE_UNIDADE padrão (555555)
     */
    public static UsuarioTestBuilder chefeUnidade() {
        return new UsuarioTestBuilder()
            .comTitulo("555555")
            .comNome("Chefe Teste")
            .comUnidade("ASSESSORIA_11")
            .comPerfil(Perfil.CHEFE_UNIDADE);
    }

    /**
     * Cria um usuário SERVIDOR padrão (666666)
     */
    public static UsuarioTestBuilder servidor() {
        return new UsuarioTestBuilder()
            .comTitulo("666666")
            .comNome("Servidor Teste")
            .comUnidade("SECAO_111")
            .comPerfil(Perfil.SERVIDOR);
    }

    public UsuarioTestBuilder comTitulo(String titulo) {
        this.titulo = titulo;
        return this;
    }

    public UsuarioTestBuilder comNome(String nome) {
        this.nome = nome;
        return this;
    }

    public UsuarioTestBuilder comUnidade(String codigoUnidade) {
        this.codigoUnidade = codigoUnidade;
        return this;
    }

    public UsuarioTestBuilder comPerfil(Perfil perfil) {
        this.perfis.add(perfil);
        return this;
    }

    public UsuarioTestBuilder comPerfis(Perfil... perfis) {
        for (Perfil perfil : perfis) {
            this.perfis.add(perfil);
        }
        return this;
    }

    public Usuario build() {
        Usuario usuario = new Usuario();
        usuario.setTitulo(titulo);
        usuario.setNome(nome);
        usuario.setCodigoUnidade(codigoUnidade);
        usuario.setPerfis(new HashSet<>(perfis));
        return usuario;
    }
}
