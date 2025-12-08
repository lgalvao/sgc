package sgc.diagnostico.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;

/**
 * DTO de resposta para diagnóstico completo.
 */
@Builder
public record DiagnosticoDto(
        Long codigo,
        Long subprocessoCodigo,
        String situacao,
        String situacaoLabel,
        LocalDateTime dataConclusao,
        String dataConclusaoFormatada,
        String justificativaConclusao,
        List<ServidorDiagnosticoDto> servidores,
        boolean podeSerConcluido,
        String motivoNaoPodeConcluir
) {
}
