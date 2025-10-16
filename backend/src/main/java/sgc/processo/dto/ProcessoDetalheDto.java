package sgc.processo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sgc.processo.SituacaoProcesso;
import sgc.subprocesso.SituacaoSubprocesso;

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
    private LocalDateTime dataLimite;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataFinalizacao;

    @Builder.Default
    private List<UnidadeParticipanteDto> unidades = new ArrayList<>();

    @Builder.Default
    private List<ProcessoResumoDto> resumoSubprocessos = new ArrayList<>();

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UnidadeParticipanteDto {
        private String nome;
        private String sigla;
        private Long codUnidade;
        private Long codUnidadeSuperior;
        private SituacaoSubprocesso situacaoSubprocesso;
        private LocalDateTime dataLimite;

        @Builder.Default
        private List<UnidadeParticipanteDto> filhos = new ArrayList<>();
    }
}