package sgc.processo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProcessoResumoDto {
    private Long codigo;
    private String descricao;
    private String situacao;
    private String tipo;
    private LocalDate dataLimite;
    private LocalDateTime dataCriacao;
    private Long unidadeCodigo;
    private String unidadeNome;
}