package sgc.model;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

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
    public static class Id implements Serializable {
        @Column(name = "atividade_codigo")
        private Long atividadeCodigo;

        @Column(name = "competencia_codigo")
        private Long competenciaCodigo;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Id id = (Id) o;
            return Objects.equals(atividadeCodigo, id.atividadeCodigo)
                    && Objects.equals(competenciaCodigo, id.competenciaCodigo);
        }

        @Override
        public int hashCode() {
            return Objects.hash(atividadeCodigo, competenciaCodigo);
        }
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
}