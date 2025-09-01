package sgc.modelo;

import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
@Table(name = "USUARIO_PERFIL_UNIDADE")
public class UsuarioPerfilUnidade extends EntidadeBase {
    @Enumerated(EnumType.STRING)
    Perfil perfil;

    @ManyToOne
    Usuario usuario;

    @ManyToOne
    Unidade unidade;
}
