package sgc.mapa.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sgc.comum.model.EntidadeBase;

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
    @jakarta.annotation.Nullable
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

    public Atividade(Mapa mapa, String descricao) {
        super();
        this.mapa = mapa;
        this.descricao = descricao;
    }
}
