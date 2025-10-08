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
public class ProcessoDto {
    private Long codigo;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataFinalizacao;
    private LocalDate dataLimite;
    private String descricao;
    private String situacao;
    private String tipo;
}