package sgc.modelo;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;

@Getter
@Entity
@Table(name = "ADMINISTRADOR")
public class Administrador extends EntidadeBase {
    @ManyToOne
    Usuario usuario;
}