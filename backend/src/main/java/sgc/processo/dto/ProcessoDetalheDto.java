package sgc.processo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProcessoDetalheDto {
    private Long codigo;
    private String descricao;
    private String tipo;
    private String situacao;
    private LocalDate dataLimite;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataFinalizacao;
    private List<UnidadeParticipanteDTO> unidades = new ArrayList<>();
    private List<ProcessoResumoDto> resumoSubprocessos = new ArrayList<>();

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UnidadeParticipanteDTO {
        private Long unidadeCodigo;
        private String nome;
        private String sigla;
        private Long unidadeSuperiorCodigo;
        private String situacaoSubprocesso;
        private LocalDate dataLimite;
        private List<UnidadeParticipanteDTO> filhos = new ArrayList<>();
    }
}