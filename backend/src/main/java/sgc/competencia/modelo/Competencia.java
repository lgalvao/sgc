package sgc.competencia.modelo;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sgc.comum.modelo.EntidadeBase;
import sgc.mapa.modelo.Mapa;

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
}
