package sgc.organizacao.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "ADMINISTRADOR", schema = "sgc")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class Administrador {
    @Id
    @Column(name = "usuario_titulo", length = 12)
    private String usuarioTitulo;
}
