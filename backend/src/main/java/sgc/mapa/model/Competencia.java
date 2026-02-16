package sgc.mapa.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import sgc.comum.model.EntidadeBase;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "COMPETENCIA", schema = "sgc")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class Competencia extends EntidadeBase {
    @ManyToOne
    @JoinColumn(name = "mapa_codigo", nullable = false)
    @JsonIgnore
    private Mapa mapa;

    @Column(name = "descricao", nullable = false)
    @JsonView(MapaViews.Publica.class)
    private String descricao;

    @ManyToMany(mappedBy = "competencias")
    @Builder.Default
    @JsonView(MapaViews.Publica.class)
    @JsonIgnoreProperties("competencias")
    private Set<Atividade> atividades = new HashSet<>();
}
