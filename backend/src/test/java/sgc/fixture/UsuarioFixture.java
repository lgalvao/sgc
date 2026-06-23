package sgc.fixture;

import sgc.organizacao.model.*;

public class UsuarioFixture {

    public static Usuario usuarioPadrao() {
        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral("123456789012");
        usuario.setMatricula("12345678"); // Campo obrigatório para VW_USUARIO
        usuario.setNome("Usuário teste");
        usuario.setEmail("teste@example.com");
        usuario.setRamal("1234");
        // Unidade lotacao default
        usuario.setUnidadeLotacao(UnidadeFixture.unidadePadrao());
        return usuario;
    }

    public static Usuario usuarioComPerfil(Unidade unidade, Perfil perfil) {
        Usuario usuario = usuarioPadrao();
        usuario.setPerfilAtivo(perfil);
        usuario.setUnidadeAtivaCodigo(unidade.getCodigo());
        return usuario;
    }

    public static Usuario usuarioComTitulo(String titulo) {
        Usuario usuario = usuarioPadrao();
        usuario.setTituloEleitoral(titulo);
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
