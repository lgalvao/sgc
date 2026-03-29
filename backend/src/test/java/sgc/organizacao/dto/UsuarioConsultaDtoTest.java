package sgc.organizacao.dto;

import org.junit.jupiter.api.*;
import sgc.organizacao.model.*;

import static org.assertj.core.api.Assertions.*;

@DisplayName("UsuarioConsultaDto")
class UsuarioConsultaDtoTest {

    @Test
    @DisplayName("deve mapear usuario com unidade e perfis vazios")
    void deveMapearUsuarioComUnidadeEPerfisVazios() {
        Unidade unidade = new Unidade();
        unidade.setCodigo(10L);
        unidade.setNome("Unidade");
        unidade.setSigla("UND");
        unidade.setTipo(TipoUnidade.OPERACIONAL);
        unidade.setTituloTitular("999");

        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral("123");
        usuario.setMatricula("0001");
        usuario.setNome("João");
        usuario.setEmail("joao@tre.jus.br");
        usuario.setRamal("1234");
        usuario.setUnidadeLotacao(unidade);

        UsuarioConsultaDto dto = UsuarioConsultaDto.fromEntity(usuario);

        assertThat(dto.tituloEleitoral()).isEqualTo("123");
        assertThat(dto.unidade()).isNotNull();
        assertThat(dto.unidade().getCodigo()).isEqualTo(10L);
        assertThat(dto.perfis()).isEmpty();
    }
}
