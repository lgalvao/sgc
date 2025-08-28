package sgc.modelo.pessoas;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sgc.modelo.base.EntidadeBase;
import sgc.modelo.base.Mapa;

import java.util.Set;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "UNIDADE")
public class Unidade extends EntidadeBase {
    String sigla;
    String nome;
    String tipo;

    @ManyToOne
    Usuario titular;

    @ManyToOne
    Responsabilidade responsabilidade;

    @ManyToOne
    Unidade unidadeSuperior;

    @OneToOne
    Mapa mapaVigente;

    @OneToMany(mappedBy = "unidadeSuperior")
    Set<Unidade> unidadesSubordinadas;
}
