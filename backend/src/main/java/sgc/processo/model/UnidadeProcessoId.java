package sgc.processo.model;

import jakarta.persistence.*;
import lombok.*;
import org.jspecify.annotations.*;

import java.io.*;

/**
 * Chave primária composta para {@link UnidadeProcesso}.
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@SuppressWarnings("NullAway.Init")
public class UnidadeProcessoId implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Column(name = "processo_codigo")
    private @Nullable Long processoCodigo;

    @Column(name = "unidade_codigo")
    private @Nullable Long unidadeCodigo;
}
