package sgc.organizacao.dto;

import org.junit.jupiter.api.*;
import sgc.organizacao.*;
import sgc.organizacao.model.*;

import static org.assertj.core.api.Assertions.*;

@DisplayName("UsuarioResumoDto")
class UsuarioResumoDtoTest {
    private final OrganizacaoDtoMapper mapper = new OrganizacaoDtoMapper();


    @Test
    @DisplayName("deve retornar nulo quando usuario for nulo")
    void deveRetornarNuloQuandoUsuarioForNulo() {
        assertThat(mapper.paraUsuarioResumo(null)).isNull();
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

        UsuarioResumoDto dto = mapper.paraUsuarioResumoObrigatorio(usuario);

        assertThat(dto.tituloEleitoral()).isEqualTo("123");
        assertThat(dto.nome()).isEqualTo("Ana");
    }
}
