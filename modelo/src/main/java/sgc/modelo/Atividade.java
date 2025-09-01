package sgc.modelo;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;

import java.util.Set;

@Getter
@Entity
@Table(name = "ATIVIDADE")
public class Atividade extends EntidadeBase {
    @ManyToOne
    Mapa mapa;

    String descricao;

    @ManyToMany(mappedBy = "atividades")
    Set<Competencia> competencias;
}
