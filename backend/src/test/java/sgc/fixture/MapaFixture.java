package sgc.fixture;

import sgc.mapa.internal.model.Mapa;
import sgc.subprocesso.internal.model.Subprocesso;

public class MapaFixture {

    public static Mapa mapaPadrao(Subprocesso subprocesso) {
        Mapa mapa = new Mapa();
        mapa.setCodigo(1L);
        mapa.setSubprocesso(subprocesso);
        return mapa;
    }
}
