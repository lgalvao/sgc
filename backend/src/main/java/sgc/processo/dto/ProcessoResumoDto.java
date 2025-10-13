package sgc.processo.dto;

import lombok.*;
import sgc.comum.modelo.SituacaoProcesso;

import java.time.LocalDate;
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
    private LocalDate dataLimite;
    private LocalDateTime dataCriacao;
    private Long unidadeCodigo;
    private String unidadeNome;
}