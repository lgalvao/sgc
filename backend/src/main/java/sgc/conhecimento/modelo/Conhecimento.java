package sgc.conhecimento.modelo;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sgc.atividade.modelo.Atividade;
import sgc.comum.modelo.EntidadeBase;

/**
 * Representa um conhecimento específico necessário para realizar uma atividade.
 */
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

    /**
     * Construtor para criar um novo conhecimento.
     * @param descricao A descrição do conhecimento.
     * @param atividade A atividade à qual o conhecimento está associado.
     */
    public Conhecimento(String descricao, Atividade atividade) {
        this.descricao = descricao;
        this.atividade = atividade;
    }

    /**
     * Retorna o código da atividade associada.
     * @return O código da atividade.
     */
    public Long getCodigoAtividade() {
        return atividade.getCodigo();
    }
}
