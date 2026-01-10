package sgc.mapa.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sgc.comum.model.EntidadeBase;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "COMPETENCIA", schema = "sgc")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Competencia extends EntidadeBase {
    @ManyToOne
    @JoinColumn(name = "mapa_codigo")
    private Mapa mapa;

    @Column(name = "descricao")
    private String descricao;

    @ManyToMany(mappedBy = "competencias")
    private Set<Atividade> atividades = new HashSet<>();

    public Competencia(Long codigo, String descricao, Mapa mapa) {
        super(codigo);
        this.descricao = descricao;
        this.mapa = mapa;
    }

    public Competencia(String descricao, Mapa mapa) {
        super();
        this.descricao = descricao;
        this.mapa = mapa;
    }
}
