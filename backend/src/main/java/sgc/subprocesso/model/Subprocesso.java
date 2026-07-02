package sgc.subprocesso.model;

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
    private Processo processo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_codigo", nullable = false)
    private Unidade unidade;

    @OneToOne(mappedBy = "subprocesso")
    private Mapa mapa;

    @Column(name = "data_limite_etapa1", nullable = false)
    private LocalDateTime dataLimiteEtapa1;

    @Column(name = "data_fim_etapa1")
    private @Nullable LocalDateTime dataFimEtapa1;

    @Column(name = "data_limite_etapa2")
    private @Nullable LocalDateTime dataLimiteEtapa2;

    @Column(name = "data_fim_etapa2")
    private @Nullable LocalDateTime dataFimEtapa2;

    @Enumerated(EnumType.STRING)
    @Column(name = "situacao", length = 50, nullable = false)
    @lombok.Builder.Default
    private SituacaoSubprocesso situacao = SituacaoSubprocesso.NAO_INICIADO;

    public Set<Atividade> getAtividades() {
        return mapa != null ? mapa.getAtividades() : Collections.emptySet();
    }

    public Long getCodProcesso() {
        return processo.getCodigo();
    }

    public @Nullable Long getCodUnidade() {
        return unidade != null ? unidade.getCodigo() : null;
    }

    public @Nullable Long getCodMapa() {
        return mapa != null ? mapa.getCodigo() : null;
    }

    public void setSituacao(SituacaoSubprocesso novaSituacao) {
        if (situacao != novaSituacao && !situacao.podeTransicionarPara(novaSituacao, processo.getTipo())) {
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
                Arrays.asList(MAPEAMENTO_MAPA_HOMOLOGADO, REVISAO_MAPA_HOMOLOGADO, DIAGNOSTICO_CONCLUIDO, DIAGNOSTICO_HOMOLOGADO);
        return !situacoesFinalizadas.contains(this.situacao) && !SituacaoSubprocesso.NAO_INICIADO.equals(this.situacao);
    }

    public @Nullable Integer getEtapaAtual() {
        final List<SituacaoSubprocesso> situacoesFinalizadas =
                Arrays.asList(MAPEAMENTO_MAPA_HOMOLOGADO, REVISAO_MAPA_HOMOLOGADO, DIAGNOSTICO_CONCLUIDO, DIAGNOSTICO_HOMOLOGADO);
        return !situacoesFinalizadas.contains(this.situacao) ? 1 : null;
    }
}
