package sgc.processo.dto;

import lombok.*;
import sgc.processo.model.SituacaoProcesso;

import java.time.LocalDateTime;

/**
 * DTO de resposta contendo dados de um processo.
 *
 * <p>Requer @NoArgsConstructor e @Setter para uso em mappers.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessoDto {

    private Long codigo;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataFinalizacao;
    private LocalDateTime dataLimite;
    private String descricao;
    private SituacaoProcesso situacao;
    private String tipo;

    // Campos formatados para apresentação
    private String dataCriacaoFormatada;
    private String dataFinalizacaoFormatada;
    private String dataLimiteFormatada;
    private String situacaoLabel;
    private String tipoLabel;
    private String unidadesParticipantes;
}
