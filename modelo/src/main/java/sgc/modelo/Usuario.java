package sgc.modelo;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;

@Getter
@Entity
@Table(name = "USUARIO")
public class Usuario {
    @Id
    String titulo;

    @ManyToOne
    Unidade unidade;

    String nome;
    String email;
    String ramal;
}
