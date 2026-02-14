package sgc.subprocesso.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import sgc.subprocesso.model.Subprocesso;

import static org.assertj.core.api.Assertions.assertThat;
import sgc.organizacao.model.Usuario;

@Tag("unit")
@DisplayName("SubprocessoDetalheMapper - Cobertura Adicional")
class SubprocessoDetalheMapperCoverageTest {

    private final SubprocessoDetalheMapper mapper = Mappers.getMapper(SubprocessoDetalheMapper.class);

    @Test
    @DisplayName("mapResponsavel deve retornar null se responsavel ou subprocesso forem nulos")
    void deveRetornarNullSeEntradasNulasNoMapResponsavel() {
        assertThat(mapper.mapResponsavel(null, null)).isNull();
        assertThat(mapper.mapResponsavel(new Subprocesso(), null)).isNull();
        assertThat(mapper.mapResponsavel(null, new Usuario())).isNull();
    }

    @Test
    @DisplayName("mapPrazoEtapaAtual deve retornar null se subprocesso for nulo")
    void deveRetornarNullSeSubprocessoNuloNoMapPrazo() {
        assertThat(mapper.mapPrazoEtapaAtual(null)).isNull();
    }
}
