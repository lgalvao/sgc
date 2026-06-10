package sgc.diagnostico.dto;

import lombok.Builder;
import sgc.subprocesso.dto.MovimentacaoDto;

import java.util.List;

@Builder
public record DiagnosticoUnidadeDto(
        UnidadeResumoDto unidade,
        String situacaoDiagnostico,
        List<ServidorDiagnosticoDto> servidores,
        List<SituacaoCapacitacaoDto> situacoesCapacitacao,
        List<MovimentacaoDto> movimentacoes
) {
}
