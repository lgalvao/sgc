package sgc.modelo.config;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import sgc.modelo.base.EntidadeBase;

@Getter
@Entity
@Table(name = "PARAMETRO")
public class Parametro extends EntidadeBase {
    String chave;
    String valor;
    String descricao;
}
