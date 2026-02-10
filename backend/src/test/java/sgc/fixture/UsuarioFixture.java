package sgc.fixture;

import sgc.organizacao.model.Perfil;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.organizacao.model.UsuarioPerfil;

import java.util.HashSet;
import java.util.Set;

public class UsuarioFixture {

    public static Usuario usuarioPadrao() {
        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral("123456789012");
        usuario.setMatricula("12345678"); // Campo obrigatório para VW_USUARIO
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

        Set<UsuarioPerfil> atribuicoes = usuario.getTodasAtribuicoes(new HashSet<>());
        atribuicoes.add(up);
    }

    public static Usuario usuarioComTitulo(String titulo) {
        Usuario usuario = usuarioPadrao();
        usuario.setTituloEleitoral(titulo);
        // Gera matrícula a partir do título de forma segura
        // Se título tem >= 8 caracteres, pega os últimos 8
        // Senão, preenche com zeros à esquerda até ter 8 caracteres
        String matricula;
        if (titulo.length() >= 8) {
            matricula = titulo.substring(titulo.length() - 8);
        } else {
            // Preenche com zeros à esquerda
            matricula = String.format("%08d", titulo.chars().sum()); // Hash simples baseado na soma dos caracteres
        }
        usuario.setMatricula(matricula);
        return usuario;
    }
}
