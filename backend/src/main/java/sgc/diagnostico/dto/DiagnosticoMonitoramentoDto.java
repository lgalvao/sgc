package sgc.diagnostico.dto;
import java.time.LocalDateTime;
import java.util.List;

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
