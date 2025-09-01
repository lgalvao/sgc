package sgc.modelo;

import jakarta.persistence.*;
import lombok.Getter;

import java.util.Set;

@Getter
@Entity
@Table(name = "UNIDADE")
public class Unidade extends EntidadeBase {
    String sigla;
    String nome;
    TipoUnidade tipo;

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

    @Enumerated
    SituacaoUnidade situacao;
}
