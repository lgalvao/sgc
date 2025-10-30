package sgc.competencia.modelo;

import jakarta.persistence.*;
import lombok.*;
import sgc.atividade.modelo.Atividade;

import java.io.Serializable;

/**
 * Entidade de associação que representa a relação N-N entre Competência e Atividade.
 */
@Entity
@Table(name = "COMPETENCIA_ATIVIDADE", schema = "sgc")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CompetenciaAtividade implements Serializable {
    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class Id implements Serializable {
        private Long codAtividade;
        private Long codCompetencia;
    }

    @EmbeddedId
    private Id id;

    @MapsId("codAtividade")
    @ManyToOne
    @JoinColumn(name = "atividade_codigo", insertable = false, updatable = false)
    private Atividade atividade;

    @MapsId("codCompetencia")
    @ManyToOne
    @JoinColumn(name = "competencia_codigo", insertable = false, updatable = false)
    private Competencia competencia;

    /**
     * Construtor para criar uma nova associação.
     *
     * @param id          O código da associação.
     * @param competencia A competência a ser associada.
     * @param atividade   A atividade a ser associada.
     */
    public CompetenciaAtividade(Id id, Competencia competencia, Atividade atividade) {
        this.id = new Id(id.getCodAtividade(), id.getCodCompetencia());
        this.competencia = new Competencia(competencia);
        this.atividade = new Atividade(atividade);
    }

    /**
     * Define o código da associação, garantindo a imutabilidade.
     *
     * @param id O código da associação.
     */
    public void setId(Id id) {
        this.id = new Id(id.getCodAtividade(), id.getCodCompetencia());
    }

    /**
     * Retorna uma cópia do código da associação para garantir a imutabilidade.
     *
     * @return O código da associação.
     */
    public Id getId() {
        return new Id(this.id.getCodAtividade(), this.id.getCodCompetencia());
    }

    /**
     * Define a competência da associação, garantindo a imutabilidade.
     *
     * @param competencia A competência.
     */
    public void setCompetencia(Competencia competencia) {
        this.competencia = new Competencia(competencia);
    }

    /**
     * Retorna uma cópia da competência para garantir a imutabilidade.
     *
     * @return A competência.
     */
    public Competencia getCompetencia() {
        return new Competencia(this.competencia);
    }

    /**
     * Define a atividade da associação, garantindo a imutabilidade.
     *
     * @param atividade A atividade.
     */
    public void setAtividade(Atividade atividade) {
        this.atividade = new Atividade(atividade);
    }

    /**
     * Retorna uma cópia da atividade para garantir a imutabilidade.
     *
     * @return A atividade.
     */
    public Atividade getAtividade() {
        return new Atividade(this.atividade);
    }
}