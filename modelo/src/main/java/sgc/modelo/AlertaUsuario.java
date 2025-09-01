package sgc.modelo;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;

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
