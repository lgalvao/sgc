package sgc.fixture;

import sgc.mapa.api.model.Competencia;
import sgc.mapa.api.model.Mapa;

public class CompetenciaFixture {

    public static Competencia competenciaPadrao(Mapa mapa) {
        return new Competencia("Competência de Teste " + System.nanoTime(), mapa);
    }
}
