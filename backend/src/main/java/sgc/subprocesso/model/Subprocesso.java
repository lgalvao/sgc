package sgc.subprocesso.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sgc.comum.model.EntidadeBase;
import sgc.mapa.model.Mapa;
import sgc.processo.model.Processo;
import sgc.unidade.model.Unidade;

import java.time.LocalDateTime;

@Entity
@Table(name = "SUBPROCESSO", schema = "sgc")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Subprocesso extends EntidadeBase {
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

    /**
     * Construtor de conveniência para criar um novo subprocesso no início de um processo.
     */
    public Subprocesso(Processo processo, Unidade unidade, Mapa mapa, SituacaoSubprocesso situacao, LocalDateTime dataLimiteEtapa1) {
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
}