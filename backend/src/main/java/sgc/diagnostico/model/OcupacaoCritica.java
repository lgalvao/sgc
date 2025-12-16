package sgc.diagnostico.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sgc.comum.model.EntidadeBase;
import sgc.mapa.model.Competencia;
import sgc.sgrh.model.Usuario;

/**
 * Ocupação crítica: situação de capacitação de um servidor para uma competência.
 * Conforme CDU-07 do DRAFT-Diagnostico.md.
 */
@Entity
@Table(name = "OCUPACAO_CRITICA",
        schema = "sgc",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"diagnostico_codigo", "servidor_titulo", "competencia_codigo"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OcupacaoCritica extends EntidadeBase {
    @ManyToOne
    @JoinColumn(name = "diagnostico_codigo", nullable = false)
    private Diagnostico diagnostico;

    @ManyToOne
    @JoinColumn(name = "servidor_titulo", nullable = false)
    private Usuario servidor;

    @ManyToOne
    @JoinColumn(name = "competencia_codigo", nullable = false)
    private Competencia competencia;

    @Enumerated(EnumType.STRING)
    @Column(name = "situacao_capacitacao", nullable = false)
    private SituacaoCapacitacao situacao;
}
