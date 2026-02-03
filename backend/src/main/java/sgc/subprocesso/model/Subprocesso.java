package sgc.subprocesso.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.jspecify.annotations.NonNull;
import sgc.comum.model.EntidadeBase;
import sgc.mapa.model.Mapa;
import sgc.organizacao.model.Unidade;
import sgc.processo.model.Processo;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Entity
@Table(name = "SUBPROCESSO", schema = "sgc")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class Subprocesso extends EntidadeBase {
    @ManyToOne
    @JoinColumn(name = "processo_codigo", nullable = false)
    private Processo processo;

    @ManyToOne
    @JoinColumn(name = "unidade_codigo", nullable = false)
    private Unidade unidade;

    @OneToOne(mappedBy = "subprocesso")
    private Mapa mapa;

    @Column(name = "data_limite_etapa1")
    private LocalDateTime dataLimiteEtapa1;

    @Column(name = "data_fim_etapa1")
    private LocalDateTime dataFimEtapa1;

    @Column(name = "data_limite_etapa2")
    private LocalDateTime dataLimiteEtapa2;

    @Column(name = "data_fim_etapa2")
    private LocalDateTime dataFimEtapa2;

    @Enumerated(EnumType.STRING)
    @Column(name = "situacao", length = 50)
    private SituacaoSubprocesso situacao;

    public void setSituacao(SituacaoSubprocesso novaSituacao) {
        if (this.situacao != null && novaSituacao != null && !this.situacao.podeTransicionarPara(novaSituacao)) {
            throw new sgc.subprocesso.erros.ErroTransicaoInvalida(
                    String.format("Transição de situação inválida: %s -> %s", this.situacao.getDescricao(), novaSituacao.getDescricao())
            );
        }
        this.situacao = novaSituacao;
    }

    public @NonNull Processo getProcesso() {
        return processo;
    }

    public @NonNull Unidade getUnidade() {
        return unidade;
    }

    public boolean isEmAndamento() {
        final List<SituacaoSubprocesso> situacoesFinalizadas =
                Arrays.asList(
                        SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO,
                        SituacaoSubprocesso.REVISAO_MAPA_HOMOLOGADO,
                        SituacaoSubprocesso.NAO_INICIADO);

        return !situacoesFinalizadas.contains(this.situacao);
    }

    public Integer getEtapaAtual() {
        final List<SituacaoSubprocesso> situacoesFinalizadas =
                Arrays.asList(
                        SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO,
                        SituacaoSubprocesso.REVISAO_MAPA_HOMOLOGADO);

        return !situacoesFinalizadas.contains(this.situacao) ? 1 : null;
    }
}
