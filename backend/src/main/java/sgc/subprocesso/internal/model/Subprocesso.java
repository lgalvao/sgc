package sgc.subprocesso.internal.model;

import jakarta.persistence.*;
import lombok.*;
import sgc.comum.model.EntidadeBase;
import sgc.mapa.api.model.Mapa;
import sgc.processo.api.model.Processo;
import sgc.unidade.api.model.Unidade;

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

    public Subprocesso(
            Long codigo,
            Processo processo,
            Unidade unidade,
            Mapa mapa,
            SituacaoSubprocesso situacao,
            LocalDateTime dataLimiteEtapa1) {
        super(codigo);
        this.processo = processo;
        this.unidade = unidade;
        this.mapa = mapa;
        this.situacao = situacao;
        this.dataLimiteEtapa1 = dataLimiteEtapa1;
    }

    /**
     * Construtor de conveniência para criar um novo subprocesso no início de um processo.
     */
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
