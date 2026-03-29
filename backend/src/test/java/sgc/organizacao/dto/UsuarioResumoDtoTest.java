package sgc.organizacao.dto;

import org.junit.jupiter.api.*;
import sgc.organizacao.model.*;

import static org.assertj.core.api.Assertions.*;

@DisplayName("UsuarioResumoDto")
class UsuarioResumoDtoTest {

    @Test
    @DisplayName("deve retornar nulo quando usuario for nulo")
    void deveRetornarNuloQuandoUsuarioForNulo() {
        assertThat(UsuarioResumoDto.fromEntity(null)).isNull();
    }

    @Test
    @DisplayName("deve mapear usuario obrigatorio")
    void deveMapearUsuarioObrigatorio() {
        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral("123");
        usuario.setMatricula("0001");
        usuario.setNome("Ana");
        usuario.setEmail("ana@tre.jus.br");
        usuario.setRamal("1234");

        UsuarioResumoDto dto = UsuarioResumoDto.fromEntityObrigatorio(usuario);

        assertThat(dto.tituloEleitoral()).isEqualTo("123");
        assertThat(dto.nome()).isEqualTo("Ana");
    }
}
