package sgc.organizacao.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.*;
import org.jspecify.annotations.*;

@Entity
@Table(name = "ADMINISTRADOR", schema = "sgc")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@SuppressWarnings("NullAway.Init")
public class Administrador {
    @Id
    @Column(name = "usuario_titulo", length = 12, nullable = false)
    private @Nullable String usuarioTitulo;
}
