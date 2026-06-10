package sgc.diagnostico.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.*;
import sgc.comum.model.*;
import sgc.mapa.model.*;
import sgc.organizacao.model.*;

@Entity
@Table(name = "SITUACAO_CAPACITACAO", schema = "sgc")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@AttributeOverride(name = "codigo", column = @Column(name = "codigo"))
public class SituacaoCapacitacao extends EntidadeBase {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diagnostico_codigo", nullable = false)
    private Diagnostico diagnostico;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "servidor_titulo", referencedColumnName = "titulo", nullable = false)
    private Usuario servidor;

    @Column(name = "servidor_nome_snapshot")
    private String servidorNomeSnapshot;

    @Column(name = "unidade_codigo_snapshot")
    private Long unidadeCodigoSnapshot;

    @Column(name = "unidade_sigla_snapshot", length = 20)
    private String unidadeSiglaSnapshot;

    @Column(name = "unidade_nome_snapshot")
    private String unidadeNomeSnapshot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "competencia_codigo", nullable = false)
    private Competencia competencia;

    @Enumerated(EnumType.STRING)
    @Column(name = "situacao_capacitacao", length = 10)
    private ValorSituacaoCapacitacao situacaoCapacitacao;

    public String getServidorNomeDiagnostico() {
        if (servidorNomeSnapshot != null && !servidorNomeSnapshot.isBlank()) {
            return servidorNomeSnapshot;
        }
        return servidor != null ? servidor.getNome() : null;
    }
}
