package sgc.mapa;

import jakarta.persistence.*;
import lombok.*;
import sgc.unidade.Unidade;

import java.io.Serializable;

@Entity
@Table(name = "UNIDADE_MAPA", schema = "sgc")
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
    @EqualsAndHashCode
    public static class Id implements Serializable {
        private Long unidadeCodigo;
        private Long mapaVigenteCodigo;
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