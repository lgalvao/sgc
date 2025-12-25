package sgc.processo.api;

import lombok.*;
import sgc.processo.api.model.SituacaoProcesso;

import java.time.LocalDateTime;

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
