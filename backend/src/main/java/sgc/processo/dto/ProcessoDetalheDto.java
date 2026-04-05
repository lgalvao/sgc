package sgc.processo.dto;

import lombok.*;
import org.jspecify.annotations.*;
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
        private @Nullable Long codUnidadeSuperior;
        private SituacaoSubprocesso situacaoSubprocesso;
        private LocalDateTime dataLimite;
        private @Nullable Long mapaCodigo;
        private @Nullable Long codSubprocesso;
        private @Nullable Long localizacaoAtualCodigo;

        public static UnidadeParticipanteDto fromUnidade(Unidade unidade) {
            return criarBase(
                    unidade.getNome(),
                    unidade.getSigla(),
                    unidade.getCodigo(),
                    unidade.getUnidadeSuperior() != null
                            ? unidade.getUnidadeSuperior().getCodigo()
                            : null
            );
        }

        public static UnidadeParticipanteDto fromSnapshot(UnidadeProcesso snapshot) {
            return criarBase(
                    snapshot.getNome(),
                    snapshot.getSigla(),
                    snapshot.getUnidadeCodigoPersistido(),
                    snapshot.getUnidadeSuperiorCodigo()
            );
        }

        public void preencherComSubprocesso(Subprocesso subprocesso, Unidade localizacaoAtual) {
            this.situacaoSubprocesso = subprocesso.getSituacao();
            this.dataLimite = subprocesso.getDataLimiteEtapa1();
            this.codSubprocesso = subprocesso.getCodigo();
            this.mapaCodigo = subprocesso.getMapa() != null ? subprocesso.getMapa().getCodigo() : null;
            this.localizacaoAtualCodigo = localizacaoAtual.getCodigo();
        }

        private static UnidadeParticipanteDto criarBase(
                String nome,
                String sigla,
                Long codUnidade,
                @Nullable Long codUnidadeSuperior
        ) {
            return UnidadeParticipanteDto.builder()
                    .nome(nome)
                    .sigla(sigla)
                    .codUnidade(Objects.requireNonNull(codUnidade, "Codigo da unidade participante obrigatorio"))
                    .codUnidadeSuperior(codUnidadeSuperior)
                    .filhos(new ArrayList<>())
                    .build();
        }
    }
}
