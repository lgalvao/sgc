package sgc.processo.dto;

import lombok.*;
import sgc.organizacao.model.*;
import sgc.processo.model.*;
import sgc.subprocesso.model.*;

import java.time.*;
import java.util.*;

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
    private boolean podeAceitarCadastroBloco;
    private boolean podeDisponibilizarMapaBloco;



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


        public static UnidadeParticipanteDto fromUnidade(Unidade unidade) {
            if (unidade == null) return null;
            return UnidadeParticipanteDto.builder()
                    .nome(unidade.getNome())
                    .sigla(unidade.getSigla())
                    .codUnidade(unidade.getCodigo())
                    .codUnidadeSuperior(unidade.getUnidadeSuperior() != null ? unidade.getUnidadeSuperior().getCodigo() : null)
                    .filhos(new ArrayList<>())
                    .build();
        }

        public static UnidadeParticipanteDto fromSnapshot(UnidadeProcesso snapshot) {
            if (snapshot == null) return null;
            return UnidadeParticipanteDto.builder()
                    .nome(snapshot.getNome())
                    .sigla(snapshot.getSigla())
                    .codUnidade(snapshot.getUnidadeCodigo())
                    .codUnidadeSuperior(snapshot.getUnidadeSuperiorCodigo())
                    .filhos(new ArrayList<>())
                    .build();
        }
    }
}

