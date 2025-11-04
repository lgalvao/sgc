package sgc.atividade.modelo;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sgc.comum.modelo.EntidadeBase;
import sgc.mapa.modelo.Mapa;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa uma atividade desempenhada em um determinado contexto,
 * associada a um mapa de competências.
 */
@Entity
@Table(name = "ATIVIDADE", schema = "sgc")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Atividade extends EntidadeBase {
    @ManyToOne
    @JoinColumn(name = "mapa_codigo")
    private Mapa mapa;

    @Column(name = "descricao")
    private String descricao;

    @OneToMany(mappedBy = "atividade", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Conhecimento> conhecimentos = new ArrayList<>();

    /**
     * Construtor para criar uma nova atividade.
     *
     * @param mapa      O mapa ao qual a atividade pertence.
     * @param descricao A descrição da atividade.
     */
    public Atividade(Mapa mapa, String descricao) {
        this.mapa = mapa;
        this.descricao = descricao;
    }
}
