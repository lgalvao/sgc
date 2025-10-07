package sgc.unidade;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sgc.comum.EntidadeBase;
import sgc.comum.Usuario;

import java.time.LocalDate;

@Entity
@Table(name = "ATRIBUICAO_TEMPORARIA", schema = "sgc")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AtribuicaoTemporaria extends EntidadeBase {
    @ManyToOne
    @JoinColumn(name = "unidade_codigo")
    private Unidade unidade;

    @ManyToOne
    @JoinColumn(name = "usuario_titulo")
    private Usuario usuario;

    @Column(name = "data_inicio")
    private LocalDate dataInicio;

    @Column(name = "data_termino")
    private LocalDate dataTermino;

    @Column(name = "justificativa", length = 500)
    private String justificativa;
}