package sgc.fixture;

import sgc.mapa.api.model.Mapa;
import sgc.subprocesso.internal.model.Subprocesso;

public class MapaFixture {

    public static Mapa mapaPadrao(Subprocesso subprocesso) {
        Mapa mapa = new Mapa();
        mapa.setCodigo(1L);
        if (subprocesso != null) {
            mapa.setSubprocessoCodigo(subprocesso.getCodigo());
        }
        return mapa;
    }
}
