package sgc.fixture;

import sgc.mapa.model.*;

public class CompetenciaFixture {

    public static Competencia competenciaPadrao(Mapa mapa) {
        return Competencia.builder().descricao("Competência de Teste " + System.nanoTime()).mapa(mapa).build();
    }
}
