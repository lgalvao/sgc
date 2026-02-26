package sgc.mapa.model;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.*;
import sgc.comum.model.*;

import java.util.*;

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
    @JsonIgnoreProperties("competencias")
    @JsonView(MapaViews.Publica.class)
    private Set<Atividade> atividades = new HashSet<>();
}
