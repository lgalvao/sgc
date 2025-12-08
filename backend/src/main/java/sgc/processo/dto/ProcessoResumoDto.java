package sgc.processo.dto;

import java.time.LocalDateTime;
import lombok.*;
import sgc.processo.model.SituacaoProcesso;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProcessoResumoDto {
    private Long codigo;
    private String descricao;
    private SituacaoProcesso situacao;
    private String tipo;
    private LocalDateTime dataLimite;
    private LocalDateTime dataCriacao;
    private Long unidadeCodigo;
    private String unidadeNome;
    private String unidadesParticipantes;
    private String linkDestino;
}
