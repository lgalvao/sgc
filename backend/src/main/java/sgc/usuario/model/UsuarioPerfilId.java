package sgc.usuario.model;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class UsuarioPerfilId implements Serializable {
    private String usuarioTitulo;
    private Long unidadeCodigo;
    private Perfil perfil;
}
