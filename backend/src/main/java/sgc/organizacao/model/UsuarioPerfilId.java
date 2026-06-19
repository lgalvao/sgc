package sgc.organizacao.model;

import lombok.*;
import org.jspecify.annotations.Nullable;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@SuppressWarnings("NullAway.Init")
public class UsuarioPerfilId implements Serializable {
    private @Nullable String usuarioTitulo;
    private @Nullable Long unidadeCodigo;
    private @Nullable Perfil perfil;
}
