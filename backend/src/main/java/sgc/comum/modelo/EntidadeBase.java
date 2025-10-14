package sgc.comum.modelo;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@MappedSuperclass
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public abstract class EntidadeBase implements Serializable {
    /**
     * Construtor de cópia.
     *
     * @param outra A entidade da qual os valores serão copiados.
     */
    public EntidadeBase(EntidadeBase outra) {
        if (outra != null) {
            this.codigo = outra.codigo;
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "default_seq_gen")
    @Column(name = "codigo")
    private Long codigo;
}