package sgc.subprocesso.modelo;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sgc.comum.modelo.SituacaoSubprocesso;
import sgc.comum.modelo.EntidadeBase;
import sgc.mapa.modelo.Mapa;
import sgc.processo.modelo.Processo;
import sgc.unidade.modelo.Unidade;

import java.time.LocalDate;
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
    private LocalDate dataLimiteEtapa1;

    @Column(name = "data_fim_etapa1")
    private LocalDateTime dataFimEtapa1;

    @Column(name = "data_limite_etapa2")
    private LocalDate dataLimiteEtapa2;

    @Column(name = "data_fim_etapa2")
    private LocalDateTime dataFimEtapa2;

    @Enumerated(EnumType.STRING)
    @Column(name = "situacao_id", length = 50)
    private SituacaoSubprocesso situacao;

    /**
     * Construtor de conveniência para criar um novo subprocesso no início de um processo.
     */
    public Subprocesso(Processo processo, Unidade unidade, Mapa mapa, SituacaoSubprocesso situacao, LocalDate dataLimiteEtapa1) {
        super();
        this.processo = processo;
        this.unidade = unidade;
        this.mapa = mapa;
        this.situacao = situacao;
        this.dataLimiteEtapa1 = dataLimiteEtapa1;
    }
}