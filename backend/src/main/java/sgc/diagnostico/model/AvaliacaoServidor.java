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
 * Avaliação de uma competência por um servidor no diagnóstico.
 * Inclui autoavaliação e futuramente avaliação de consenso.
 * Conforme CDU-02 e CDU-04 do DRAFT-Diagnostico.md.
 */
@Entity
@Table(
        name = "AVALIACAO_SERVIDOR",
        schema = "sgc",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"diagnostico_codigo", "servidor_titulo", "competencia_codigo"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AvaliacaoServidor extends EntidadeBase {

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
    @Column(name = "importancia")
    private NivelAvaliacao importancia;

    @Enumerated(EnumType.STRING)
    @Column(name = "dominio")
    private NivelAvaliacao dominio;

    @Column(name = "gap")
    private Integer gap;

    @Column(name = "observacoes", columnDefinition = "TEXT")
    private String observacoes;

    @Enumerated(EnumType.STRING)
    @Column(name = "situacao_servidor", nullable = false)
    private SituacaoServidorDiagnostico situacao;

    /**
     * Construtor de conveniência para criar uma nova avaliação.
     * Situação inicial: AUTOAVALIACAO_NAO_REALIZADA.
     */
    public AvaliacaoServidor(
            Diagnostico diagnostico,
            Usuario servidor,
            Competencia competencia) {
        this.diagnostico = diagnostico;
        this.servidor = servidor;
        this.competencia = competencia;
        this.situacao = SituacaoServidorDiagnostico.AUTOAVALIACAO_NAO_REALIZADA;
    }

    /**
     * Calcula o gap entre importância e domínio.
     * Gap = importancia.valor - dominio.valor
     * Se algum for NA, gap é null.
     */
    public void calcularGap() {
        if (importancia != null && dominio != null
                && importancia != NivelAvaliacao.NA && dominio != NivelAvaliacao.NA) {
            this.gap = importancia.getValor() - dominio.getValor();
        } else {
            this.gap = null;
        }
    }
}
