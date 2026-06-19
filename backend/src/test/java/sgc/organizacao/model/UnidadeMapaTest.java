package sgc.organizacao.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("UnidadeMapa")
class UnidadeMapaTest {

    @Test
    @DisplayName("deve retornar codigo persistido quando presente")
    void deveRetornarCodigoPersistidoQuandoPresente() {
        UnidadeMapa unidadeMapa = new UnidadeMapa();
        unidadeMapa.setUnidadeCodigo(8L);

        assertThat(unidadeMapa.getUnidadeCodigoPersistido()).isEqualTo(8L);
    }

    @Test
    @DisplayName("deve falhar quando codigo persistido estiver ausente")
    void deveFalharQuandoCodigoPersistidoEstiverAusente() {
        UnidadeMapa unidadeMapa = new UnidadeMapa();

        assertThatThrownBy(unidadeMapa::getUnidadeCodigoPersistido)
                .isInstanceOf(sgc.comum.erros.ErroInconsistenciaInterna.class)
                .hasMessage("UnidadeMapa sem unidadeCodigo persistido");
    }
}
