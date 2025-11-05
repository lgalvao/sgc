package sgc.mapa.model;

import jakarta.persistence.*;
import lombok.*;
import sgc.atividade.model.Atividade;

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

    /**
     * -- SETTER --
     * Define a atividade da associação, garantindo a imutabilidade.
     *
     */
    @MapsId("codAtividade")
    @ManyToOne
    @JoinColumn(name = "atividade_codigo", insertable = false, updatable = false)
    private Atividade atividade;

    /**
     * -- GETTER --
     * Retorna uma cópia da competência para garantir a imutabilidade.
     *
     */
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
        this.competencia = competencia;
        this.atividade = atividade;
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
}