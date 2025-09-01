package sgc.modelo;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;

@Getter
@Entity
@Table(name = "PARAMETRO")
public class Parametro extends EntidadeBase {
    String chave;
    String valor;
    String descricao;
}
