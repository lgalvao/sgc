package sgc.modelo.pessoas;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import sgc.modelo.base.EntidadeBase;

@Getter
@Entity
@Table(name = "USUARIO")
public class Usuario extends EntidadeBase {
    @ManyToOne
    Unidade unidadeLotacao;

    String nome;
    String email;
    String ramal;
}
