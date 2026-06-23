package sgc.organizacao.model;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

@DisplayName("UsuarioPerfil")
class UsuarioPerfilTest {

    @Test
    @DisplayName("deve comparar identidade por usuario unidade e perfil")
    void deveCompararIdentidadePorUsuarioUnidadeEPerfil() {
        UsuarioPerfil primeiro = UsuarioPerfil.builder()
                .usuarioTitulo("123")
                .unidadeCodigo(10L)
                .perfil(Perfil.ADMIN)
                .build();

        UsuarioPerfil segundo = UsuarioPerfil.builder()
                .usuarioTitulo("123")
                .unidadeCodigo(10L)
                .perfil(Perfil.ADMIN)
                .build();

        UsuarioPerfil terceiro = UsuarioPerfil.builder()
                .usuarioTitulo("123")
                .unidadeCodigo(10L)
                .perfil(Perfil.GESTOR)
                .build();

        assertThat(primeiro).isEqualTo(segundo);
        assertThat(primeiro).hasSameHashCodeAs(segundo);
        assertThat(primeiro).isNotEqualTo(terceiro);
    }
}
