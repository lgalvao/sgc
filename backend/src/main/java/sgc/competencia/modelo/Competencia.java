package sgc.competencia.modelo;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sgc.comum.modelo.EntidadeBase;
import sgc.mapa.modelo.Mapa;

/**
 * Representa uma competência, conjunto de atividades e conhecimentos.
 */
@Entity
@Table(name = "COMPETENCIA", schema = "sgc")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Competencia extends EntidadeBase {
    @ManyToOne
    @JoinColumn(name = "mapa_codigo")
    private Mapa mapa;

    @Column(name = "descricao")
    private String descricao;

    /**
     * Construtor para criar uma nova competência.
     *
     * @param descricao A descrição da competência.
     * @param mapa      O mapa ao qual a competência pertence.
     */
    public Competencia(String descricao, Mapa mapa) {
        this.descricao = descricao;
        this.mapa = mapa;
    }

    /**
     * Construtor de cópia.
     *
     * @param competencia A competência a ser copiada.
     */
    public Competencia(Competencia competencia) {
        if (competencia != null) {
            super.setCodigo(competencia.getCodigo());
            this.mapa = competencia.getMapa();
            this.descricao = competencia.getDescricao();
        }
    }

}
