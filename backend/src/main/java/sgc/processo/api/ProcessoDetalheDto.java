package sgc.processo.api;

import lombok.*;
import sgc.processo.internal.model.SituacaoProcesso;
import sgc.subprocesso.internal.model.SituacaoSubprocesso;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProcessoDetalheDto {
    @Builder.Default
    private final List<UnidadeParticipanteDto> unidades = new ArrayList<>();
    @Builder.Default
    private final List<ProcessoResumoDto> resumoSubprocessos = new ArrayList<>();
    private Long codigo;
    private String descricao;
    private String tipo;
    private SituacaoProcesso situacao;
    private LocalDateTime dataLimite;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataFinalizacao;
    private boolean podeFinalizar;
    private boolean podeHomologarCadastro;
    private boolean podeHomologarMapa;

    // Campos formatados para apresentação
    private String dataLimiteFormatada;
    private String dataCriacaoFormatada;
    private String dataFinalizacaoFormatada;
    private String situacaoLabel;
    private String tipoLabel;

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UnidadeParticipanteDto {
        @Builder.Default
        private final List<UnidadeParticipanteDto> filhos = new ArrayList<>();
        private String nome;
        private String sigla;
        private Long codUnidade;
        private Long codUnidadeSuperior;
        private SituacaoSubprocesso situacaoSubprocesso;
        private LocalDateTime dataLimite;
        private Long mapaCodigo;
        private Long codSubprocesso;
        // Campos formatados para apresentação
        private String dataLimiteFormatada;
        private String situacaoLabel;
    }
}

