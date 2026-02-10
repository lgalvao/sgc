package sgc.processo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

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
    @Column(name = "processo_codigo")
    private Long processoCodigo;
    
    @Column(name = "unidade_codigo")
    private Long unidadeCodigo;
}
