package sgc.fixture;

import sgc.atividade.api.model.Atividade;
import sgc.mapa.api.model.Mapa;

public class AtividadeFixture {

    public static Atividade atividadePadrao(Mapa mapa) {
        Atividade atividade = new Atividade();
        // Remove fixed ID for persistence tests
        // atividade.setCodigo(1L);
        atividade.setMapa(mapa);
        atividade.setDescricao("Atividade de Teste");
        return atividade;
    }
}
