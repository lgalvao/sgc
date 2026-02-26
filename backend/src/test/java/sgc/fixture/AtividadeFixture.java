package sgc.fixture;

import sgc.mapa.model.*;

public class AtividadeFixture {
    public static Atividade atividadePadrao(Mapa mapa) {
        Atividade atividade = new Atividade();
        atividade.setMapa(mapa);
        atividade.setDescricao("Atividade de Teste");
        return atividade;
    }
}
