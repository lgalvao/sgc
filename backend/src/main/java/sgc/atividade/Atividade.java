package sgc.atividade;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sgc.comum.EntidadeBase;
import sgc.mapa.Mapa;

@Entity
@Table(name = "ATIVIDADE", schema = "sgc")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Atividade extends EntidadeBase {

    @ManyToOne
    @JoinColumn(name = "mapa_codigo")
    private Mapa mapa;

    @Column(name = "descricao")
    private String descricao;
}
