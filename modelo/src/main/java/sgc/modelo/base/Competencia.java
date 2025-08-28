package sgc.modelo.base;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;

import java.util.Set;

@Getter
@Entity
@Table(name = "COMPETENCIA")
public class Competencia extends EntidadeBase {
    @ManyToOne
    Mapa mapa;

    String descricao;

    @ManyToMany
    Set<Atividade> atividades;
}
