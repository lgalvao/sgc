package sgc.comum.model;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

@DisplayName("EntidadeBaseTest")
class EntidadeBaseTest {

    private static class EntidadeTeste extends EntidadeBase {
    }

    @Test
    @DisplayName("deve gerar toString com nome da classe e código")
    void deveGerarToStringComNomeClasseECodigo() {
        EntidadeTeste entidade = new EntidadeTeste();
        entidade.setCodigo(42L);

        assertThat(entidade.toString()).isEqualTo("EntidadeTeste[codigo=42]");
    }

    @Test
    @DisplayName("deve comparar igualdade por código")
    void deveCompararIgualdadePorCodigo() {
        EntidadeTeste primeira = new EntidadeTeste();
        primeira.setCodigo(42L);
        EntidadeTeste segunda = new EntidadeTeste();
        segunda.setCodigo(42L);

        assertThat(primeira).isEqualTo(segunda);
        assertThat(primeira.hashCode()).isEqualTo(segunda.hashCode());
    }

    @Test
    @DisplayName("não deve considerar igual quando código for nulo ou tipo for diferente")
    void naoDeveConsiderarIgualQuandoCodigoNuloOuTipoDiferente() {
        EntidadeTeste entidade = new EntidadeTeste();

        assertThat(entidade.equals("outro")).isFalse();
        assertThat(entidade.equals(new EntidadeTeste())).isFalse();
    }
}
