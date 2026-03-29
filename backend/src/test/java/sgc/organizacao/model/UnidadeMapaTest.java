package sgc.organizacao.model;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

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
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("UnidadeMapa sem unidadeCodigo persistido");
    }
}
