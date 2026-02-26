package sgc.organizacao.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.*;

@Entity
@Table(name = "ADMINISTRADOR", schema = "sgc")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class Administrador {
    @Id
    @Column(name = "usuario_titulo", length = 12, nullable = false)
    private String usuarioTitulo;
}
