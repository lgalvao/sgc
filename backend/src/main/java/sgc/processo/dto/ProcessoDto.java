package sgc.processo.dto;

import lombok.*;
import sgc.processo.SituacaoProcesso;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProcessoDto {
    private Long codigo;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataFinalizacao;
    private LocalDate dataLimite;
    private String descricao;
    private SituacaoProcesso situacao;
    private String tipo;
}