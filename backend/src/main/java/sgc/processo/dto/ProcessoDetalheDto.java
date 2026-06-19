package sgc.processo.dto;

import lombok.*;

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

    @Builder.Default
    private final List<SubprocessoElegivelDto> elegiveis = new ArrayList<>();

    @Builder.Default
    private final List<AcaoBlocoDto> acoesBloco = new ArrayList<>();
    private Long codigo;
    private String descricao;
    private String tipo;
    private String situacao;
    private LocalDateTime dataLimite;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataFinalizacao;
    private boolean podeFinalizar;
    private boolean podeHomologarCadastro;
    private boolean podeHomologarMapa;
    private boolean podeAceitarCadastroBloco;
    private boolean podeAceitarMapaBloco;
    private boolean podeDisponibilizarMapaBloco;

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AcaoBlocoDto {
        @Builder.Default
        private final List<SubprocessoElegivelDto> unidades = new ArrayList<>();
        private String codigo;
        private String acao;
        private boolean mostrar;
        private boolean habilitar;
        private boolean requerDataLimite;
        private boolean redirecionarPainel;
        private String rotulo;
        private String titulo;
        private String texto;
        private String rotuloBotao;
        private String mensagemSucesso;
    }

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
        private String situacaoSubprocesso;
        private LocalDateTime dataLimite;
        private Long mapaCodigo;
        private Long codSubprocesso;
        private Long localizacaoAtualCodigo;
    }
}
