package sesel.sgc.historico;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import sesel.sgc.EntidadeBase;

import java.util.List;

@Entity
@Table(name = "historico")
@Getter
@Setter
public class Historico extends EntidadeBase {
    @OneToMany(mappedBy = "historico", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<EventoHistorico> eventos;
}
