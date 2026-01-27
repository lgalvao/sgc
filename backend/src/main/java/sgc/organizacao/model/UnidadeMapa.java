package sgc.organizacao.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import sgc.mapa.model.Mapa;

@Entity
@Table(name = "UNIDADE_MAPA", schema = "sgc")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class UnidadeMapa {
    @Id
    @Column(name = "unidade_codigo")
    private Long unidadeCodigo;

    @ManyToOne
    @JoinColumn(name = "mapa_vigente_codigo", nullable = false)
    private Mapa mapaVigente;
}
