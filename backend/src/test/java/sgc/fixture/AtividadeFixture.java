package sgc.fixture;

import sgc.atividade.model.Atividade;
import sgc.mapa.model.Mapa;

public class AtividadeFixture {

    public static Atividade atividadePadrao(Mapa mapa) {
        Atividade atividade = new Atividade();
        atividade.setCodigo(1L);
        atividade.setMapa(mapa);
        atividade.setDescricao("Atividade de Teste");
        return atividade;
    }
}
