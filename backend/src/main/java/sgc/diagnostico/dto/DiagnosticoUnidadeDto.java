package sgc.diagnostico.dto;

import sgc.subprocesso.dto.MovimentacaoDto;

import java.util.List;

public record DiagnosticoUnidadeDto(
        UnidadeResumoDto unidade,
        List<ServidorDiagnosticoDto> servidores,
        List<OcupacaoCriticaDto> ocupacoesCriticas,
        List<MovimentacaoDto> movimentacoes
) {
}
