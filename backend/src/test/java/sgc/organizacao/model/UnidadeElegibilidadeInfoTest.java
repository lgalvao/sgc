package sgc.organizacao.model;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class UnidadeElegibilidadeInfoTest {

    @Test
    void deveRetornarTrueQuandoPossuiResponsavelEfetivo() {
        var info = new UnidadeElegibilidadeInfo(1L, TipoUnidade.OPERACIONAL, "Chefe");
        assertThat(info.possuiResponsavelEfetivo()).isTrue();
    }

    @Test
    void deveRetornarFalseQuandoResponsavelForNulo() {
        var info = new UnidadeElegibilidadeInfo(1L, TipoUnidade.OPERACIONAL, null);
        assertThat(info.possuiResponsavelEfetivo()).isFalse();
    }

    @Test
    void deveRetornarFalseQuandoResponsavelForVazio() {
        var info = new UnidadeElegibilidadeInfo(1L, TipoUnidade.OPERACIONAL, "");
        assertThat(info.possuiResponsavelEfetivo()).isFalse();
    }

    @Test
    void deveRetornarFalseQuandoResponsavelForApenasEspacos() {
        var info = new UnidadeElegibilidadeInfo(1L, TipoUnidade.OPERACIONAL, "   ");
        assertThat(info.possuiResponsavelEfetivo()).isFalse();
    }
}
