package sgc.modelo.acomp;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import sgc.modelo.pessoas.Usuario;
import sgc.modelo.base.EntidadeBase;

@Getter
@Entity
@Table(name = "ALERTA_USUARIO")
public class AlertaUsuario extends EntidadeBase {
    @ManyToOne
    Alerta alerta;

    @ManyToOne
    Usuario usuario;

    boolean lido = false;
}
