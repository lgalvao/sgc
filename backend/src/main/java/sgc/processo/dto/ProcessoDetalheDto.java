package sgc.processo.dto;

import lombok.*;
import sgc.processo.model.SituacaoProcesso;
import sgc.subprocesso.model.SituacaoSubprocesso;

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
    private final List<UnidadeParticipanteDto> unidades = new ArrayList<>();

    @Builder.Default
    private final List<ProcessoResumoDto> resumoSubprocessos = new ArrayList<>();

    private boolean podeFinalizar;
    private boolean podeHomologarCadastro;
    private boolean podeHomologarMapa;

    @Getter
    @Setter
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
        private Long mapaCodigo;
        private Long codSubprocesso;

        @Builder.Default
        private final List<UnidadeParticipanteDto> filhos = new ArrayList<>();
    }
}