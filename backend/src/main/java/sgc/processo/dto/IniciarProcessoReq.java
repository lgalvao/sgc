package sgc.processo.dto;

import sgc.processo.model.TipoProcesso;

import java.util.List;

public record IniciarProcessoReq(TipoProcesso tipo, List<Long> unidades) {}
