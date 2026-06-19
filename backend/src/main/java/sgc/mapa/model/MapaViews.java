package sgc.mapa.model;

import sgc.comum.model.ComumViews;

/**
 * Views para serialização JSON do módulo Mapa.
 */
public final class MapaViews {
    private MapaViews() {
    }

    /**
     * View mínima, contendo apenas identificadores e descrições básicas.
     */
    public interface Minimal extends ComumViews.Publica {
    }

    /**
     * View pública, contendo todos os dados básicos da entidade.
     */
    public interface Publica extends Minimal {
    }
}
