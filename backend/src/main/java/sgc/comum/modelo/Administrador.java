package sgc.comum.modelo;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sgc.sgrh.modelo.Usuario;

import java.io.Serializable;

/**
 * Representa um administrador do sistema.
 * <p>
 * A entidade Administrador estende as informações de um {@link Usuario},
 * marcando-o com privilégios de administrador no sistema SGC.
 */
@Entity
@Table(name = "ADMINISTRADOR", schema = "sgc")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Administrador implements Serializable {
    @Id
    @Column(name = "usuario_titulo", length = 12)
    private String usuarioTitulo;

    @OneToOne
    @MapsId
    @JoinColumn(name = "usuario_titulo")
    private Usuario usuario;

}