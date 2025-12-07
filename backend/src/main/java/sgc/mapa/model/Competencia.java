package sgc.mapa.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sgc.atividade.model.Atividade;
import sgc.comum.model.EntidadeBase;

import java.util.HashSet;
import java.util.Set;

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
    public Competencia(Long codigo, String descricao, Mapa mapa) {
        super(codigo);
        this.descricao = descricao;
        this.mapa = mapa;
    }

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
        super();
        this.descricao = descricao;
        this.mapa = mapa;
    }

    @ManyToMany(mappedBy = "competencias")
    private Set<Atividade> atividades = new HashSet<>();
}
