package sgc.processo.dto;

import lombok.*;
import sgc.comum.modelo.SituacaoProcesso;
import sgc.comum.modelo.SituacaoSubprocesso;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProcessoDetalheDto {
    private Long codigo;
    private String descricao;
    private String tipo;
    private SituacaoProcesso situacao;
    private LocalDate dataLimite;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataFinalizacao;
    @Builder.Default
    private List<UnidadeParticipanteDTO> unidades = new ArrayList<>();
    @Builder.Default
    private List<ProcessoResumoDto> resumoSubprocessos = new ArrayList<>();

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UnidadeParticipanteDTO {
        private Long unidadeCodigo;
        private String nome;
        private String sigla;
        private Long unidadeSuperiorCodigo;
        private SituacaoSubprocesso situacaoSubprocesso;
        private LocalDate dataLimite;
        @Builder.Default
        private List<UnidadeParticipanteDTO> filhos = new ArrayList<>();
    }
}