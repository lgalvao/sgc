package sgc.subprocesso.model;

import sgc.mapa.model.*;
import sgc.organizacao.model.*;

public final class SubprocessoViews {
    private SubprocessoViews() {
    }

    public interface Publica extends MapaViews.Publica, OrganizacaoViews.Publica {
    }
}
