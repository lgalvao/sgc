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
        this.id = id;
        this.competencia = competencia;
        this.atividade = atividade;
    }
}