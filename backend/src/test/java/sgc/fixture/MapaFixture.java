package sgc.fixture;

import org.jspecify.annotations.*;
import sgc.mapa.model.*;
import sgc.subprocesso.model.*;

public class MapaFixture {

    public static Mapa mapaPadrao(@Nullable Subprocesso subprocesso) {
        Mapa mapa = new Mapa();
        mapa.setCodigo(1L);
        mapa.setSubprocesso(subprocesso != null ? subprocesso : new Subprocesso());
        return mapa;
    }
}
