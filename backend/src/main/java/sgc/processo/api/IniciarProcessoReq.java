package sgc.processo.api;

import sgc.processo.api.model.TipoProcesso;

import java.util.List;

public record IniciarProcessoReq(TipoProcesso tipo, List<Long> unidades) {
}
