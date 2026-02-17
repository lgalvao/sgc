package sgc.mapa.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import sgc.comum.model.EntidadeBase;

import java.util.HashSet;
import java.util.LinkedHashSet;
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
    @JsonIgnore
    private Mapa mapa;

    @JsonView(MapaViews.Publica.class)
    @Column(name = "descricao", nullable = false)
    private String descricao;

    @OneToMany(mappedBy = "atividade", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonView(MapaViews.Publica.class)
    private Set<Conhecimento> conhecimentos = new LinkedHashSet<>();

    @ManyToMany
    @JoinTable(
            name = "competencia_atividade",
            schema = "sgc",
            joinColumns = @JoinColumn(name = "atividade_codigo"),
            inverseJoinColumns = @JoinColumn(name = "competencia_codigo"))
    @Builder.Default
    @JsonView(MapaViews.Publica.class)
    @JsonIgnoreProperties("atividades")
    private Set<Competencia> competencias = new HashSet<>();

    @JsonView(MapaViews.Publica.class)
    @JsonProperty("mapaCodigo")
    public Long getMapaCodigo() {
        return mapa.getCodigo();
    }
}
