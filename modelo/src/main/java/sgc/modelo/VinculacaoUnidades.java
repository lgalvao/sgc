package sgc.modelo;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;

@Getter
@Entity
@Table(name = "VINCULACAO_UNIDADE")
public class VinculacaoUnidades extends EntidadeBase {
    @ManyToOne
    Unidade unidadeAnteror;

    @ManyToOne
    Unidade unidadeAtual;
}