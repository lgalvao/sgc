package sgc.diagnostico.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sgc.comum.model.EntidadeBase;
import sgc.subprocesso.model.Subprocesso;

import java.time.LocalDateTime;

/**
 * Entidade que representa o diagnóstico de competências de uma unidade.
 * Um diagnóstico está associado a um subprocesso de tipo DIAGNOSTICO.
 * Conforme DRAFT-Diagnostico.md.
 */
@Entity
@Table(name = "DIAGNOSTICO", schema = "sgc")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Diagnostico extends EntidadeBase {

    @OneToOne
    @JoinColumn(name = "subprocesso_codigo", unique = true, nullable = false)
    private Subprocesso subprocesso;

    @Enumerated(EnumType.STRING)
    @Column(name = "situacao", nullable = false)
    private SituacaoDiagnostico situacao;

    @Column(name = "data_conclusao")
    private LocalDateTime dataConclusao;

    @Column(name = "justificativa_conclusao", columnDefinition = "TEXT")
    private String justificativaConclusao;

    /**
     * Construtor de conveniência para criar um novo diagnóstico.
     * Situação inicial: EM_ANDAMENTO.
     */
    public Diagnostico(Subprocesso subprocesso) {
        this.subprocesso = subprocesso;
        this.situacao = SituacaoDiagnostico.EM_ANDAMENTO;
    }
}
