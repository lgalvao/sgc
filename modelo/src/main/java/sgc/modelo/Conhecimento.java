package sgc.modelo;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;

@Getter
@Entity
@Table(name = "CONHECIMENTO")
public class Conhecimento extends EntidadeBase {
    String descricao;

    @ManyToOne
    Atividade atividade;
}
