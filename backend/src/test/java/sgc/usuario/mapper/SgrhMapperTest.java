package sgc.usuario.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import sgc.usuario.mapper.UsuarioMapper;
import sgc.unidade.dto.UnidadeDto;
import sgc.usuario.dto.UsuarioDto;
import sgc.usuario.model.Usuario;
import sgc.unidade.model.Unidade;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Testes de Mapper: UsuarioMapper")
class UsuarioMapperTest {

    private final UsuarioMapper mapper = Mappers.getMapper(UsuarioMapper.class);

    @Test
    @DisplayName("Deve mapear Unidade para UnidadeDto corretamente")
    void deveMapearParaUnidadeDtoCorretamente() {
        // Arrange
        Unidade unidade = new Unidade();
        unidade.setCodigo(1L);
        unidade.setNome("Unidade Teste");
        unidade.setSigla("UT");

        // Act
        UnidadeDto dto = mapper.toUnidadeDto(unidade);

        // Assert
        assertThat(dto).isNotNull();
        assertThat(dto.getCodigo()).isEqualTo(1L);
        assertThat(dto.getNome()).isEqualTo("Unidade Teste");
        assertThat(dto.getSigla()).isEqualTo("UT");
        assertThat(dto.isElegivel()).isTrue();
    }

    @Test
    @DisplayName("Deve mapear Usuario para UsuarioDto corretamente")
    void deveMapearParaUsuarioDtoCorretamente() {
        // Arrange
        Unidade unidade = new Unidade();
        unidade.setCodigo(10L);

        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral("123");
        usuario.setNome("Usuário Teste");
        usuario.setEmail("teste@email.com");
        usuario.setMatricula("MAT001");
        usuario.setUnidadeLotacao(unidade);

        // Act
        UsuarioDto dto = mapper.toUsuarioDto(usuario);

        // Assert
        assertThat(dto).isNotNull();
        assertThat(dto.getTituloEleitoral()).isEqualTo("123");
        assertThat(dto.getNome()).isEqualTo("Usuário Teste");
        assertThat(dto.getEmail()).isEqualTo("teste@email.com");
        assertThat(dto.getMatricula()).isEqualTo("MAT001");
        assertThat(dto.getUnidadeCodigo()).isEqualTo(10L);
    }
}
