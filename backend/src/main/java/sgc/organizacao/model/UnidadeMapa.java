package sgc.organizacao.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.*;
import org.jspecify.annotations.*;
import sgc.mapa.model.*;

@Entity
@Table(name = "UNIDADE_MAPA", schema = "sgc")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@SuppressWarnings("NullAway.Init")
public class UnidadeMapa {
    @Id
    @Column(name = "unidade_codigo", nullable = false)
    private @Nullable Long unidadeCodigo;

    @ManyToOne
    @JoinColumn(name = "mapa_vigente_codigo", nullable = false)
    private @Nullable Mapa mapaVigente;
}
