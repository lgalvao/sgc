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
}
