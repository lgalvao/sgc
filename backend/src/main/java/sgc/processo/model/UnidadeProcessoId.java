package sgc.processo.model;

import jakarta.persistence.*;
import lombok.*;

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
public class UnidadeProcessoId implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    
    @Column(name = "processo_codigo")
    private Long processoCodigo;
    
    @Column(name = "unidade_codigo")
    private Long unidadeCodigo;
}
