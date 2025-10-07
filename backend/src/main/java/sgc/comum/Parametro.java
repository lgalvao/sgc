package sgc.comum;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "PARAMETRO", schema = "sgc")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Parametro extends EntidadeBase {

    @Column(name = "chave", length = 50)
    private String chave;

    @Column(name = "descricao")
    private String descricao;

    @Column(name = "valor")
    private String valor;
}