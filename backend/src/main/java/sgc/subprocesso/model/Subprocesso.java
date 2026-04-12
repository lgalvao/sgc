package sgc.subprocesso.model;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.*;
import org.jspecify.annotations.*;
import sgc.comum.*;
import sgc.comum.erros.*;
import sgc.comum.model.*;
import sgc.mapa.model.*;
import sgc.organizacao.model.*;
import sgc.processo.model.*;

import java.time.*;
import java.util.*;

import static sgc.subprocesso.model.SituacaoSubprocesso.*;

@Entity
@Table(name = "SUBPROCESSO", schema = "sgc")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@SuppressWarnings("NullAway.Init")
public class Subprocesso extends EntidadeBase {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processo_codigo", nullable = false)
    @JsonView({ComumViews.Publica.class, MapaViews.Publica.class})
    private Processo processo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_codigo", nullable = false)
    @JsonView({ComumViews.Publica.class, MapaViews.Publica.class})
    private Unidade unidade;

    @OneToOne(mappedBy = "subprocesso", fetch = FetchType.LAZY)
    @JsonView({ComumViews.Publica.class, MapaViews.Publica.class})
    private Mapa mapa;

    @JsonView({ComumViews.Publica.class, MapaViews.Publica.class})
    @Column(name = "data_limite_etapa1", nullable = false)
    private LocalDateTime dataLimiteEtapa1;

    @JsonView({ComumViews.Publica.class, MapaViews.Publica.class})
    @Column(name = "data_fim_etapa1")
    private @Nullable LocalDateTime dataFimEtapa1;

    @JsonView({ComumViews.Publica.class, MapaViews.Publica.class})
    @Column(name = "data_limite_etapa2")
    private @Nullable LocalDateTime dataLimiteEtapa2;

    @JsonView({ComumViews.Publica.class, MapaViews.Publica.class})
    @Column(name = "data_fim_etapa2")
    private @Nullable LocalDateTime dataFimEtapa2;

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
    public @Nullable Long getCodProcesso() {
        return processo != null ? processo.getCodigo() : null;
    }

    @JsonView(ComumViews.Publica.class)
    @JsonProperty("codUnidade")
    public @Nullable Long getCodUnidade() {
        return unidade != null ? unidade.getCodigo() : null;
    }

    @JsonView(ComumViews.Publica.class)
    @JsonProperty("codMapa")
    public @Nullable Long getCodMapa() {
        return mapa != null ? mapa.getCodigo() : null;
    }

    public void setSituacao(SituacaoSubprocesso novaSituacao) {
        if (processo != null && situacao != null && situacao != novaSituacao && !situacao.podeTransicionarPara(novaSituacao, processo.getTipo())) {
            throw new ErroValidacao(Mensagens.TRANSICAO_INVALIDA.formatted(
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

    public @Nullable Integer getEtapaAtual() {
        final List<SituacaoSubprocesso> situacoesFinalizadas =
                Arrays.asList(MAPEAMENTO_MAPA_HOMOLOGADO, REVISAO_MAPA_HOMOLOGADO, DIAGNOSTICO_CONCLUIDO);
        return !situacoesFinalizadas.contains(this.situacao) ? 1 : null;
    }
}
