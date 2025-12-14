package sgc.sgrh.mapper;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import sgc.sgrh.dto.ServidorDto;
import sgc.sgrh.dto.UnidadeDto;
import sgc.sgrh.model.Usuario;
import sgc.unidade.model.Unidade;

import static org.assertj.core.api.Assertions.assertThat;

class SgrhMapperTest {

    private final SgrhMapper mapper = Mappers.getMapper(SgrhMapper.class);

    @Test
    void toUnidadeDto() {
        Unidade unidade = new Unidade();
        unidade.setCodigo(1L);
        unidade.setNome("Unidade Teste");
        unidade.setSigla("UT");

        UnidadeDto dto = mapper.toUnidadeDto(unidade);

        assertThat(dto).isNotNull();
        assertThat(dto.getCodigo()).isEqualTo(1L);
        assertThat(dto.getNome()).isEqualTo("Unidade Teste");
        assertThat(dto.getSigla()).isEqualTo("UT");
        assertThat(dto.isElegivel()).isTrue();
    }

    @Test
    void toServidorDto() {
        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral("123");
        usuario.setNome("Servidor Teste");
        usuario.setEmail("teste@email.com");

        Unidade unidade = new Unidade();
        unidade.setCodigo(10L);
        usuario.setUnidadeLotacao(unidade);

        ServidorDto dto = mapper.toServidorDto(usuario);

        assertThat(dto).isNotNull();
        assertThat(dto.getCodigo()).isEqualTo("123");
        assertThat(dto.getNome()).isEqualTo("Servidor Teste");
        assertThat(dto.getEmail()).isEqualTo("teste@email.com");
        assertThat(dto.getUnidadeCodigo()).isEqualTo(10L);
    }
}
