package sgc.sgrh.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ADMINISTRADOR", schema = "sgc")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Administrador {
    @Id
    @Column(name = "usuario_titulo", length = 12)
    private String usuarioTitulo;
}
