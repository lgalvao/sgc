package sgc.subprocesso.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.jspecify.annotations.NonNull;
import sgc.comum.model.ComumViews;
import sgc.comum.model.EntidadeBase;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Mapa;
import sgc.mapa.model.MapaViews;
import sgc.organizacao.model.Unidade;
import sgc.processo.model.Processo;
import sgc.subprocesso.erros.ErroTransicaoInvalida;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "SUBPROCESSO", schema = "sgc")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
// TODO muitas verificações de nulo nessa classe nao fazem sentido algum. A garantia de nulidade vem antes e ja teria quebrado ao chegar aqui.
public class Subprocesso extends EntidadeBase {
    @ManyToOne
    @JoinColumn(name = "processo_codigo", nullable = false)
    @JsonView({ComumViews.Publica.class, MapaViews.Publica.class})
    private Processo processo;

    @ManyToOne
    @JoinColumn(name = "unidade_codigo", nullable = false)
    @JsonView({ComumViews.Publica.class, MapaViews.Publica.class})
    private Unidade unidade;

    @OneToOne(mappedBy = "subprocesso")
    @JsonView({ComumViews.Publica.class, MapaViews.Publica.class})
    private Mapa mapa;

    @JsonView({ComumViews.Publica.class, MapaViews.Publica.class})
    @Column(name = "data_limite_etapa1", nullable = false)
    private LocalDateTime dataLimiteEtapa1;

    @JsonView({ComumViews.Publica.class, MapaViews.Publica.class})
    @Column(name = "data_fim_etapa1")
    private LocalDateTime dataFimEtapa1;

    @JsonView({ComumViews.Publica.class, MapaViews.Publica.class})
    @Column(name = "data_limite_etapa2")
    private LocalDateTime dataLimiteEtapa2;

    @JsonView({ComumViews.Publica.class, MapaViews.Publica.class})
    @Column(name = "data_fim_etapa2")
    private LocalDateTime dataFimEtapa2;

    @JsonView({ComumViews.Publica.class, MapaViews.Publica.class})
    @Enumerated(EnumType.STRING)
    @Column(name = "situacao", length = 50, nullable = false)
    @lombok.Builder.Default
    private SituacaoSubprocesso situacao = SituacaoSubprocesso.NAO_INICIADO;

    @JsonView({ComumViews.Publica.class, SubprocessoViews.Publica.class, MapaViews.Publica.class})
    public Set<Atividade> getAtividades() {
        return mapa != null ? mapa.getAtividades() : Collections.emptySet();
    }

    @JsonView(ComumViews.Publica.class)
    @JsonProperty("codProcesso")
    public Long getCodProcesso() {
        return processo != null ? processo.getCodigo() : null;
    }

    @JsonView(ComumViews.Publica.class)
    @JsonProperty("codUnidade")
    public Long getCodUnidade() {
        return unidade != null ? unidade.getCodigo() : null;
    }

    @JsonView(ComumViews.Publica.class)
    @JsonProperty("codMapa")
    public Long getCodMapa() {
        return mapa != null ? mapa.getCodigo() : null;
    }

    public void setSituacao(SituacaoSubprocesso novaSituacao) {
        if (processo != null && situacao != null && situacao != novaSituacao && !situacao.podeTransicionarPara(novaSituacao, processo.getTipo())) {
            throw new ErroTransicaoInvalida("Transição de situação inválida: %s -> %s".formatted(
                    situacao.getDescricao(), novaSituacao.getDescricao())
            );
        }
        situacao = novaSituacao;
    }

    public void setSituacaoForcada(SituacaoSubprocesso novaSituacao) {
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
                        SituacaoSubprocesso.DIAGNOSTICO_CONCLUIDO,
                        SituacaoSubprocesso.NAO_INICIADO);

        return !situacoesFinalizadas.contains(this.situacao);
    }

    public Integer getEtapaAtual() {
        final List<SituacaoSubprocesso> situacoesFinalizadas =
                Arrays.asList(
                        SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO,
                        SituacaoSubprocesso.REVISAO_MAPA_HOMOLOGADO,
                        SituacaoSubprocesso.DIAGNOSTICO_CONCLUIDO);

        return !situacoesFinalizadas.contains(this.situacao) ? 1 : null;
    }
}
