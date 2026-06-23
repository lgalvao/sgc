package sgc.diagnostico.dto;

import java.time.*;
import java.util.*;

public record DiagnosticoMonitoramentoDto(
        Long processoCodigo,
        List<Item> unidades
) {
    public record Item(
            Long unidadeCodigo,
            String unidadeSigla,
            String unidadeNome,
            String situacaoSubprocesso,
            LocalDateTime dataLimite,
            Long localizacaoAtualCodigo
    ) {}
}
