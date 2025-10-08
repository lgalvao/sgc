package sgc.analise.modelo;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sgc.comum.modelo.EntidadeBase;
import sgc.subprocesso.modelo.Subprocesso;

import java.time.LocalDateTime;

@Entity
@Table(name = "ANALISE_VALIDACAO", schema = "sgc")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AnaliseValidacao extends EntidadeBase {
    @ManyToOne
    @JoinColumn(name = "subprocesso_codigo")
    private Subprocesso subprocesso;

    @Column(name = "data_hora")
    private LocalDateTime dataHora;

    @Column(name = "observacoes", length = 500)
    private String observacoes;
}