package sgc.diagnostico.dto;

import lombok.*;
import sgc.subprocesso.dto.*;

import java.util.*;

@Builder
public record DiagnosticoUnidadeDto(
        UnidadeResumoDto unidade,
        String situacaoDiagnostico,
        List<ServidorDiagnosticoDto> servidores,
        List<SituacaoCapacitacaoDto> situacoesCapacitacao,
        List<MovimentacaoDto> movimentacoes
) {
}
