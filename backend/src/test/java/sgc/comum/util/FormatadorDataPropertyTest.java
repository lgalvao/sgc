package sgc.comum.util;

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;

import java.time.LocalDateTime;

class FormatadorDataPropertyTest {

    @Property
    void formatacaoDeDataNuncaRetornaNuloOuVazio(@ForAll LocalDateTime data) {
        String resultado = FormatadorData.formatarData(data);
        assert resultado != null;
        assert !resultado.isEmpty();
    }

    @Property
    void formatacaoDeDataNulaRetornaHifen() {
        String resultado = FormatadorData.formatarData(null);
        assert "-".equals(resultado);
    }

    @Property
    void formatacaoDeDataHoraNuncaRetornaNuloOuVazio(@ForAll LocalDateTime data) {
        String resultado = FormatadorData.formatarDataHora(data);
        assert resultado != null;
        assert !resultado.isEmpty();
    }
}
