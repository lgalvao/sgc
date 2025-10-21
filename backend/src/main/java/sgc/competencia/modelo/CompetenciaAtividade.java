package sgc.competencia.modelo;

import jakarta.persistence.*;
import lombok.*;
import sgc.atividade.modelo.Atividade;

import java.io.Serializable;

@SuppressWarnings("PMD.DataClass")
@Entity
@Table(name = "COMPETENCIA_ATIVIDADE", schema = "sgc")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
/**
 * Entidade de associação que representa a relação N-N entre Competência e Atividade.
 */
public class CompetenciaAtividade implements Serializable {
    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    /**
     * Chave primária composta para a entidade {@link CompetenciaAtividade}.
     */
    public static class Id implements Serializable {
        private Long atividadeCodigo;
        private Long competenciaCodigo;
    }

    @EmbeddedId
    private Id id;

    @MapsId("atividadeCodigo")
    @ManyToOne
    @JoinColumn(name = "atividade_codigo", insertable = false, updatable = false)
    private Atividade atividade;

    @MapsId("competenciaCodigo")
    @ManyToOne
    @JoinColumn(name = "competencia_codigo", insertable = false, updatable = false)
    private Competencia competencia;

    /**
     * Construtor para criar uma nova associação.
     * @param id O ID da associação.
     * @param competencia A competência a ser associada.
     * @param atividade A atividade a ser associada.
     */
    public CompetenciaAtividade(Id id, Competencia competencia, Atividade atividade) {
        this.id = new Id(id.getAtividadeCodigo(), id.getCompetenciaCodigo());
        this.competencia = new Competencia(competencia);
        this.atividade = new Atividade(atividade);
    }

    /**
     * Define o ID da associação, garantindo a imutabilidade.
     * @param id O ID da associação.
     */
    public void setId(Id id) {
        this.id = new Id(id.getAtividadeCodigo(), id.getCompetenciaCodigo());
    }

    /**
     * Retorna uma cópia do ID da associação para garantir a imutabilidade.
     * @return O ID da associação.
     */
    public Id getId() {
        return new Id(this.id.getAtividadeCodigo(), this.id.getCompetenciaCodigo());
    }

    /**
     * Define a competência da associação, garantindo a imutabilidade.
     * @param competencia A competência.
     */
    public void setCompetencia(Competencia competencia) {
        this.competencia = new Competencia(competencia);
    }

    /**
     * Retorna uma cópia da competência para garantir a imutabilidade.
     * @return A competência.
     */
    public Competencia getCompetencia() {
        return new Competencia(this.competencia);
    }

    /**
     * Define a atividade da associação, garantindo a imutabilidade.
     * @param atividade A atividade.
     */
    public void setAtividade(Atividade atividade) {
        this.atividade = new Atividade(atividade);
    }

    /**
     * Retorna uma cópia da atividade para garantir a imutabilidade.
     * @return A atividade.
     */
    public Atividade getAtividade() {
        return new Atividade(this.atividade);
    }
}