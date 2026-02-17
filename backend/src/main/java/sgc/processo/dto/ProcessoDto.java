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
    private String situacao;
    private String tipo;

    private String unidadesParticipantes;
}
