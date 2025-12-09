package sgc.diagnostico.dto;

import lombok.Builder;

import java.util.List;

/**
 * DTO de resposta para dados de um servidor no diagnóstico.
 * Conforme CDU-03 (monitoramento de servidores).
 */
@Builder
public record ServidorDiagnosticoDto(
        String tituloEleitoral,
        String nome,
        String situacao,
        String situacaoLabel,
        List<AvaliacaoServidorDto> avaliacoes,
        List<OcupacaoCriticaDto> ocupacoes,
        int totalCompetencias,
        int competenciasAvaliadas,
        int ocupacoesPreenchidas
) {
}
