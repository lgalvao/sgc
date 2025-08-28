package sgc.modelo.pessoas;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import sgc.modelo.base.EntidadeBase;

import java.time.LocalDate;

@Getter
@Entity
@Table(name = "ATRIBUICAO_TEMPORARIA")
public class AtribuicaoTemporaria extends EntidadeBase {
    @ManyToOne
    Usuario servidor;

    @ManyToOne
    Unidade unidade;

    LocalDate dataInicio;
    LocalDate dataTermino;
    String justificativa;
}