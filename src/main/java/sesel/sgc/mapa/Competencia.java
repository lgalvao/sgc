package sesel.sgc.mapa;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import sesel.sgc.EntidadeBase;
import sesel.sgc.mapa.atividades.Atividade;

import java.util.List;

@Entity
@Table(name = "competencia")
@Getter
@Setter
public class Competencia extends EntidadeBase {
    @Column(nullable = false)
    private String descricao;

    @Column(columnDefinition = "TEXT")
    private String detalhamento;

    @ManyToOne
    @JoinColumn(name = "mapa_unidade_id")
    private MapaUnidade mapaUnidade;

    @OneToMany(mappedBy = "competencia", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Atividade> atividades;
}
