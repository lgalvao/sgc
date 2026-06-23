package sgc.fixture;

import sgc.mapa.model.*;
import sgc.subprocesso.model.*;

public class MapaFixture {

    public static Mapa mapaPadrao(Subprocesso subprocesso) {
        Mapa mapa = new Mapa();
        mapa.setCodigo(1L);
        mapa.setSubprocesso(subprocesso);
        return mapa;
    }
}
