package sgc.atividade.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sgc.comum.model.EntidadeBase;
import sgc.mapa.api.model.Competencia;
import sgc.mapa.api.model.Mapa;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Representa uma atividade desempenhada em um determinado contexto, associada a um mapa de
 * competências.
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
    @ManyToMany
    @JoinTable(
            name = "competencia_atividade",
            schema = "sgc",
            joinColumns = @JoinColumn(name = "atividade_codigo"),
            inverseJoinColumns = @JoinColumn(name = "competencia_codigo"))
    private Set<Competencia> competencias = new HashSet<>();

    /**
     * Construtor para criar uma nova atividade.
     *
     * @param mapa      O mapa ao qual a atividade pertence.
     * @param descricao A descrição da atividade.
     */
    public Atividade(Mapa mapa, String descricao) {
        super();
        this.mapa = mapa;
        this.descricao = descricao;
    }
}
