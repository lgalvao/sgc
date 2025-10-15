package sgc.conhecimento.modelo;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sgc.atividade.modelo.Atividade;
import sgc.comum.modelo.EntidadeBase;

@Entity
@Table(name = "CONHECIMENTO", schema = "sgc")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Conhecimento extends EntidadeBase {
    @ManyToOne
    @JoinColumn(name = "atividade_codigo")
    private Atividade atividade;

    @Column(name = "descricao")
    private String descricao;

    public Conhecimento(String descricao, Atividade atividade) {
        this.descricao = descricao;
        this.atividade = atividade;
    }

    public Long getCodigoAtividade() {
        return atividade.getCodigo();
    }
}
