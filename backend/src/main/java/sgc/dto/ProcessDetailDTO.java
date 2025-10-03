package sgc.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import sgc.dto.ProcessoSummaryDTO;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProcessDetailDTO {
    private Long codigo;
    private String descricao;
    private String tipo;
    private String situacao;
    private LocalDate dataLimite;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataFinalizacao;

    private List<UnitParticipantDTO> unidades = new ArrayList<>();

    // resumo de subprocessos (reusar ProcessoSummaryDTO)
    private List<ProcessoSummaryDTO> resumoSubprocessos = new ArrayList<>();

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UnitParticipantDTO {
        private Long unidadeCodigo;
        private String nome;
        private String sigla;
        private Long unidadeSuperiorCodigo;
        private String situacaoSubprocesso;
        private LocalDate dataLimite;
        private List<UnitParticipantDTO> filhos = new ArrayList<>();
    }
}