package sgc.unidade.modelo;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sgc.comum.modelo.EntidadeBase;

@Entity
@Table(name = "VINCULACAO_UNIDADE", schema = "sgc")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VinculacaoUnidade extends EntidadeBase {
    @ManyToOne
    @JoinColumn(name = "unidade_anterior_codigo")
    private Unidade unidadeAnterior;

    @ManyToOne
    @JoinColumn(name = "unidade_atual_codigo")
    private Unidade unidadeAtual;
}