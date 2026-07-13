package sgc.organizacao.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.*;
import sgc.mapa.model.*;

@Entity
@Table(name = "UNIDADE_MAPA", schema = "sgc")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder

public class UnidadeMapa {
    @Id
    @Column(name = "unidade_codigo", nullable = false)
    private Long unidadeCodigo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mapa_vigente_codigo", nullable = false)
    private Mapa mapaVigente;

    public Long getUnidadeCodigoPersistido() {
        return unidadeCodigo;
    }
}
