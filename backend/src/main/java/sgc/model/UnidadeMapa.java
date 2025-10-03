package sgc.model;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "UNIDADE_MAPA")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UnidadeMapa implements Serializable {

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Id implements Serializable {
        @Column(name = "unidade_codigo")
        private Long unidadeCodigo;
    
        @Column(name = "mapa_vigente_codigo")
        private Long mapaVigenteCodigo;
    
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Id id = (Id) o;
            return Objects.equals(unidadeCodigo, id.unidadeCodigo) &&
                   Objects.equals(mapaVigenteCodigo, id.mapaVigenteCodigo);
        }
    
        @Override
        public int hashCode() {
            return Objects.hash(unidadeCodigo, mapaVigenteCodigo);
        }
    }

    @EmbeddedId
    private Id id;

    @MapsId("unidadeCodigo")
    @ManyToOne
    @JoinColumn(name = "unidade_codigo", insertable = false, updatable = false)
    private Unidade unidade;

    @MapsId("mapaVigenteCodigo")
    @ManyToOne
    @JoinColumn(name = "mapa_vigente_codigo", insertable = false, updatable = false)
    private Mapa mapaVigente;
}