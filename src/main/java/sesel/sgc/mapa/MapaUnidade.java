package sesel.sgc.mapa;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import sesel.sgc.EntidadeBase;
import sesel.sgc.unidade.Unidade;

import java.util.List;

@Entity
@Table(name = "mapa_unidade")
@Getter
@Setter
public class MapaUnidade extends EntidadeBase {
    @ManyToOne
    @JoinColumn(name = "unidade_id", nullable = false)
    private Unidade unidade;

    @OneToMany(mappedBy = "mapaUnidade", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Competencia> competencias;
}
