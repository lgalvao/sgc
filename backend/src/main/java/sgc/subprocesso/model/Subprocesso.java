package sgc.subprocesso.model;

import jakarta.persistence.*;
import lombok.*;
import sgc.comum.model.EntidadeBase;
import sgc.mapa.model.Mapa;
import sgc.processo.model.Processo;
import sgc.unidade.model.Unidade;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Entity
@Table(name = "SUBPROCESSO", schema = "sgc")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subprocesso extends EntidadeBase {
    public Subprocesso(
            Long codigo,
            sgc.processo.model.Processo processo,
            sgc.unidade.model.Unidade unidade,
            sgc.mapa.model.Mapa mapa,
            SituacaoSubprocesso situacao,
            java.time.LocalDateTime dataLimiteEtapa1) {
        super(codigo);
        this.processo = processo;
        this.unidade = unidade;
        this.mapa = mapa;
        this.situacao = situacao;
        this.dataLimiteEtapa1 = dataLimiteEtapa1;
    }

    @ManyToOne
    @JoinColumn(name = "processo_codigo")
    private Processo processo;

    @ManyToOne
    @JoinColumn(name = "unidade_codigo")
    private Unidade unidade;

    @ManyToOne
    @JoinColumn(name = "mapa_codigo")
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
    @Column(name = "situacao_id", length = 50)
    private SituacaoSubprocesso situacao;

    /** Construtor de conveniência para criar um novo subprocesso no início de um processo. */
    public Subprocesso(
            Processo processo,
            Unidade unidade,
            Mapa mapa,
            SituacaoSubprocesso situacao,
            LocalDateTime dataLimiteEtapa1) {
        super();
        this.processo = processo;
        this.unidade = unidade;
        this.mapa = mapa;
        this.situacao = situacao;
        this.dataLimiteEtapa1 = dataLimiteEtapa1;
    }

    public Mapa getMapa() {
        return this.mapa == null ? null : this.mapa;
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
        if (!situacoesFinalizadas.contains(this.situacao)) {
            return 1;
        }
        return null;
    }
}
