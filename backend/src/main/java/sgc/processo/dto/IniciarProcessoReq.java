package sgc.processo.dto;

import java.util.List;
import sgc.processo.model.TipoProcesso;

public record IniciarProcessoReq(TipoProcesso tipo, List<Long> unidades) {}
