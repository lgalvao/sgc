package sgc.fixture;

import sgc.mapa.model.Competencia;
import sgc.mapa.model.Mapa;

public class CompetenciaFixture {

    public static Competencia competenciaPadrao(Mapa mapa) {
        return new Competencia("Competência de Teste " + System.nanoTime(), mapa);
    }
}
