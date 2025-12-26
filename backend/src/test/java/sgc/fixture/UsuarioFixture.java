package sgc.fixture;

import sgc.usuario.model.Perfil;
import sgc.usuario.model.Usuario;
import sgc.usuario.model.UsuarioPerfil;
import sgc.unidade.model.Unidade;

import java.util.HashSet;
import java.util.Set;

public class UsuarioFixture {

    public static Usuario usuarioPadrao() {
        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral("123456789012");
        usuario.setNome("Usuário Teste");
        usuario.setEmail("teste@example.com");
        usuario.setRamal("1234");
        // Unidade Lotacao Default
        usuario.setUnidadeLotacao(UnidadeFixture.unidadePadrao());
        return usuario;
    }

    public static Usuario usuarioComPerfil(Unidade unidade, Perfil perfil) {
        Usuario usuario = usuarioPadrao();
        adicionarPerfil(usuario, unidade, perfil);
        return usuario;
    }

    public static void adicionarPerfil(Usuario usuario, Unidade unidade, Perfil perfil) {
        UsuarioPerfil up = new UsuarioPerfil();
        up.setUsuario(usuario);
        up.setUsuarioTitulo(usuario.getTituloEleitoral());
        up.setUnidade(unidade);
        up.setUnidadeCodigo(unidade.getCodigo());
        up.setPerfil(perfil);

        Set<UsuarioPerfil> atribuicoes = usuario.getAtribuicoes();
        if (atribuicoes == null || atribuicoes.isEmpty()) {
            atribuicoes = new HashSet<>();
        }
        atribuicoes.add(up);
        usuario.setAtribuicoes(atribuicoes);
    }

    public static Usuario usuarioComTitulo(String titulo) {
        Usuario usuario = usuarioPadrao();
        usuario.setTituloEleitoral(titulo);
        return usuario;
    }
}
