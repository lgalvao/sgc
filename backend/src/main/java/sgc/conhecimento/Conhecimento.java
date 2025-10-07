package sgc.conhecimento;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sgc.atividade.Atividade;
import sgc.comum.EntidadeBase;

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
}
