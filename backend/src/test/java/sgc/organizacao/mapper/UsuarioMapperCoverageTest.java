package sgc.organizacao.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sgc.organizacao.dto.UnidadeDto;
import sgc.organizacao.dto.UsuarioDto;
import sgc.organizacao.model.TipoUnidade;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UsuarioMapper Coverage Tests")
class UsuarioMapperCoverageTest {

    private final UsuarioMapper mapper = new UsuarioMapperImpl();

    @Test
    @DisplayName("Deve retornar null quando parâmetros são nulos")
    void deveRetornarNull() {
        assertThat(mapper.toUnidadeDto(null, true)).isNull();
        assertThat(mapper.toUsuarioDto(null)).isNull();
        assertThat(mapper.toAtribuicaoTemporariaDto(null)).isNull();
    }

    @Test
    @DisplayName("Deve cobrir toUnidadeDtoComElegibilidadeCalculada")
    void deveCobrirElegibilidade() {


        Unidade u1 = Unidade.builder().codigo(1L).tipo(TipoUnidade.INTERMEDIARIA).build();
        UnidadeDto d1 = mapper.toUnidadeDtoComElegibilidadeCalculada(u1);
        assertThat(d1.isElegivel()).isFalse();

        Unidade u2 = Unidade.builder().codigo(2L).tipo(TipoUnidade.OPERACIONAL).build();
        UnidadeDto d2 = mapper.toUnidadeDtoComElegibilidadeCalculada(u2);
        assertThat(d2.isElegivel()).isTrue();
    }

    @Test
    @DisplayName("Deve cobrir toUnidadeDto com campos nulos")
    void deveCobrirUnidadeCamposNulos() {
        Unidade u = new Unidade();
        UnidadeDto dto = mapper.toUnidadeDto(u, true);
        assertThat(dto).isNotNull();
        assertThat(dto.getCodigoPai()).isNull();
        assertThat(dto.getTipo()).isNull();
    }

    @Test
    @DisplayName("Deve cobrir toUsuarioDto com campos nulos")
    void deveCobrirUsuarioCamposNulos() {
        Usuario u = new Usuario();
        UsuarioDto dto = mapper.toUsuarioDto(u);
        assertThat(dto).isNotNull();
        assertThat(dto.unidadeCodigo()).isNull();
    }
}
