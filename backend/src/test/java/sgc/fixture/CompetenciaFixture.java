package sgc.fixture;

import sgc.mapa.internal.model.Competencia;
import sgc.mapa.internal.model.Mapa;

public class CompetenciaFixture {

    public static Competencia competenciaPadrao(Mapa mapa) {
        return new Competencia("Competência de Teste " + System.nanoTime(), mapa);
    }
}
