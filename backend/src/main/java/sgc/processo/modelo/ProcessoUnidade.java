package sgc.processo.modelo;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sgc.comum.modelo.EntidadeBase;
import sgc.unidade.modelo.Unidade;

@Entity
@Table(name = "PROCESSO_UNIDADE", schema = "sgc")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProcessoUnidade extends EntidadeBase {

    @ManyToOne
    @JoinColumn(name = "processo_codigo")
    private Processo processo;

    @ManyToOne
    @JoinColumn(name = "unidade_codigo")
    private Unidade unidade;
}