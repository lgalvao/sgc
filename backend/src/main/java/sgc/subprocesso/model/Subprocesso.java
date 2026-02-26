package sgc.subprocesso.model;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.*;
import sgc.comum.model.*;
import sgc.mapa.model.*;
import sgc.organizacao.model.*;
import sgc.processo.model.*;
import sgc.subprocesso.erros.*;

import java.time.*;
import java.util.*;

import static sgc.subprocesso.model.SituacaoSubprocesso.*;

@Entity
@Table(name = "SUBPROCESSO", schema = "sgc")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
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

    @Transient
    @JsonIgnore
    private Unidade localizacaoAtual;

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

    public boolean isEmAndamento() {
        final List<SituacaoSubprocesso> situacoesFinalizadas =
                Arrays.asList(MAPEAMENTO_MAPA_HOMOLOGADO, REVISAO_MAPA_HOMOLOGADO, DIAGNOSTICO_CONCLUIDO);
        return !situacoesFinalizadas.contains(this.situacao) && !SituacaoSubprocesso.NAO_INICIADO.equals(this.situacao);
    }

    public Integer getEtapaAtual() {
        final List<SituacaoSubprocesso> situacoesFinalizadas =
                Arrays.asList(MAPEAMENTO_MAPA_HOMOLOGADO, REVISAO_MAPA_HOMOLOGADO, DIAGNOSTICO_CONCLUIDO);
        return !situacoesFinalizadas.contains(this.situacao) ? 1 : null;
    }
}
