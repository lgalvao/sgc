package sgc.mapa.model;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
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
@SuperBuilder
public class Atividade extends EntidadeBase {
    @ManyToOne
    @JoinColumn(name = "mapa_codigo", nullable = false)
    private Mapa mapa;

    @Column(name = "descricao", nullable = false)
    private String descricao;

    @OneToMany(mappedBy = "atividade", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Conhecimento> conhecimentos = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "competencia_atividade",
            schema = "sgc",
            joinColumns = @JoinColumn(name = "atividade_codigo"),
            inverseJoinColumns = @JoinColumn(name = "competencia_codigo"))
    @Builder.Default
    private Set<Competencia> competencias = new HashSet<>();
}
