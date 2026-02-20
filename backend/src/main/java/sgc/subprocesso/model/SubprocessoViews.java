package sgc.subprocesso.model;

import sgc.mapa.model.MapaViews;
import sgc.organizacao.model.OrganizacaoViews;

public final class SubprocessoViews {
    private SubprocessoViews() {
    }

    public interface Publica extends MapaViews.Publica, OrganizacaoViews.Publica {
    }
}
