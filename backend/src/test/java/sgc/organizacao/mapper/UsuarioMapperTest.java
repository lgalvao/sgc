package sgc.organizacao.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import sgc.organizacao.dto.UnidadeDto;
import sgc.organizacao.dto.UsuarioDto;
import sgc.organizacao.model.TipoUnidade;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;

import static org.assertj.core.api.Assertions.assertThat;

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
        unidade.setTipo(TipoUnidade.OPERACIONAL);

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


        // 2. Cenário: Unidade Operacional (Elegível)
        Unidade uOp = new Unidade();
        uOp.setCodigo(1L);
        uOp.setTipo(TipoUnidade.OPERACIONAL);

        UnidadeDto dtoOp = mapper.toUnidadeDtoComElegibilidadeCalculada(uOp);
        assertThat(dtoOp).isNotNull();
        assertThat(dtoOp.isElegivel()).isTrue();

        // 3. Cenário: Unidade Intermediária (Inelegível)
        Unidade uInt = new Unidade();
        uInt.setCodigo(2L);
        uInt.setTipo(TipoUnidade.INTERMEDIARIA);

        UnidadeDto dtoInt = mapper.toUnidadeDtoComElegibilidadeCalculada(uInt);
        assertThat(dtoInt).isNotNull();
        assertThat(dtoInt.isElegivel()).isFalse();
    }


    @Test
    @DisplayName("Deve retornar null quando parâmetros são nulos")
    void deveRetornarNullQuandoNulos() {
        assertThat(mapper.toUnidadeDto(null, true)).isNull();
        assertThat(mapper.toUsuarioDto(null)).isNull();
        assertThat(mapper.toAtribuicaoTemporariaDto(null)).isNull();
    }

    @Test
    @DisplayName("Deve cobrir toUnidadeDtoComElegibilidadeCalculada para cobertura extra")
    void deveCobrirElegibilidadeExtra() {
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
