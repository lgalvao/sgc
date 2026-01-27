package sgc.organizacao.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import sgc.organizacao.dto.UnidadeDto;
import sgc.organizacao.dto.UsuarioDto;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;

@Tag("unit")
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
        unidade.setTipo(sgc.organizacao.model.TipoUnidade.OPERACIONAL);

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
        assertThat(dto.tituloEleitoral()).isEqualTo("123");
        assertThat(dto.nome()).isEqualTo("Usuário Teste");
        assertThat(dto.email()).isEqualTo("teste@email.com");
        assertThat(dto.matricula()).isEqualTo("MAT001");
        assertThat(dto.unidadeCodigo()).isEqualTo(10L);
    }

    @Test
    @DisplayName("Deve mapear Unidade com elegibilidade calculada (Default Method)")
    void deveMapearComElegibilidadeCalculada() {
        // 1. Cenário: Unidade Nula
        assertThat(mapper.toUnidadeDtoComElegibilidadeCalculada(null)).isNull();

        // 2. Cenário: Unidade Operacional (Elegível)
        Unidade uOp = new Unidade();
        uOp.setCodigo(1L);
        uOp.setTipo(sgc.organizacao.model.TipoUnidade.OPERACIONAL);

        UnidadeDto dtoOp = mapper.toUnidadeDtoComElegibilidadeCalculada(uOp);
        assertThat(dtoOp).isNotNull();
        assertThat(dtoOp.isElegivel()).isTrue();

        // 3. Cenário: Unidade Intermediária (Inelegível)
        Unidade uInt = new Unidade();
        uInt.setCodigo(2L);
        uInt.setTipo(sgc.organizacao.model.TipoUnidade.INTERMEDIARIA);

        UnidadeDto dtoInt = mapper.toUnidadeDtoComElegibilidadeCalculada(uInt);
        assertThat(dtoInt).isNotNull();
        assertThat(dtoInt.isElegivel()).isFalse();
    }
}
