package sgc.unidade.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sgc.mapa.api.model.Mapa;

@Entity
@Table(name = "UNIDADE_MAPA", schema = "sgc")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UnidadeMapa {
    @Id
    @Column(name = "unidade_codigo")
    private Long unidadeCodigo;

    @ManyToOne
    @JoinColumn(name = "mapa_vigente_codigo", nullable = false)
    private Mapa mapaVigente;
}
