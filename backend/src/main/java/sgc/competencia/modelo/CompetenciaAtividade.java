package sgc.competencia.modelo;

import jakarta.persistence.*;
import lombok.*;
import sgc.atividade.modelo.Atividade;

import java.io.Serializable;

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

    public CompetenciaAtividade(Id id, Competencia competencia, Atividade atividade) {
        this.id = new Id(id.getAtividadeCodigo(), id.getCompetenciaCodigo());
        this.competencia = new Competencia(competencia);
        this.atividade = new Atividade(atividade);
    }

    public void setId(Id id) {
        this.id = new Id(id.getAtividadeCodigo(), id.getCompetenciaCodigo());
    }

    public Id getId() {
        return new Id(this.id.getAtividadeCodigo(), this.id.getCompetenciaCodigo());
    }

    public void setCompetencia(Competencia competencia) {
        this.competencia = new Competencia(competencia);
    }

    public Competencia getCompetencia() {
        return new Competencia(this.competencia);
    }

    public void setAtividade(Atividade atividade) {
        this.atividade = new Atividade(atividade);
    }

    public Atividade getAtividade() {
        return new Atividade(this.atividade);
    }
}