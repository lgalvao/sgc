package sgc.fixture;

import sgc.mapa.model.Atividade;
import sgc.mapa.model.Mapa;

public class AtividadeFixture {
    public static Atividade atividadePadrao(Mapa mapa) {
        Atividade atividade = new Atividade();
        atividade.setMapa(mapa);
        atividade.setDescricao("Atividade de Teste");
        return atividade;
    }
}
