package sgc.mapa.model;

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
    private Mapa mapa;

    @Column(name = "descricao", nullable = false)
    private String descricao;

    @ManyToMany(mappedBy = "competencias")
    @Builder.Default
    private Set<Atividade> atividades = new HashSet<>();
}
