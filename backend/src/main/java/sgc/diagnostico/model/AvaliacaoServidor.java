package sgc.diagnostico.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.*;
import org.jspecify.annotations.*;
import sgc.comum.model.*;
import sgc.mapa.model.*;
import sgc.organizacao.model.*;

@Entity
@Table(name = "AVALIACAO_SERVIDOR", schema = "sgc")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@AttributeOverride(name = "codigo", column = @Column(name = "codigo"))
public class AvaliacaoServidor extends EntidadeBase {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diagnostico_codigo", nullable = false)
    private Diagnostico diagnostico;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "servidor_titulo", referencedColumnName = "titulo", nullable = false)
    private Usuario servidor;

    @Column(name = "servidor_nome_snapshot")
    private String servidorNomeSnapshot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "competencia_codigo", nullable = false)
    private Competencia competencia;

    @Column(name = "autoimportancia")
    private Integer autoimportancia;

    @Column(name = "autodominio")
    private Integer autodominio;

    @Column(name = "chefia_importancia")
    private Integer chefiaImportancia;

    @Column(name = "chefia_dominio")
    private Integer chefiaDominio;

    @Column(name = "consenso_importancia")
    private Integer consensoImportancia;

    @Column(name = "consenso_dominio")
    private Integer consensoDominio;

    @Column(name ="importancia")
    private Integer importancia;

    @Column(name = "dominio")
    private Integer dominio;

    @Column(name = "gap")
    private @Nullable Integer gap;

    @Column(name = "observacoes")
    private String observacao;

    @Enumerated(EnumType.STRING)
    @Column(name = "situacao_servidor", length = 50, nullable = false)
    private SituacaoAvaliacaoServidor situacaoServidor;

    @Enumerated(EnumType.STRING)
    @Column(name = "situacao_servidor_anterior", length = 50)
    private SituacaoAvaliacaoServidor situacaoServidorAnterior;

    public void calculaGap() {
        if (importancia == null || dominio == null || importancia == 0 || dominio == 0) {
            gap = null;
            return;
        }
        gap = importancia - dominio;
    }

    public String getServidorNomeDiagnostico() {
        if (servidorNomeSnapshot != null && !servidorNomeSnapshot.isBlank()) {
            return servidorNomeSnapshot;
        }
        return servidor != null ? servidor.getNome() : null;
    }
}
