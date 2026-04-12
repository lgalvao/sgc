package sgc.subprocesso.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.*;
import org.jspecify.annotations.*;
import sgc.comum.model.*;

import java.time.*;

/**
 * Representa um registro de análise realizado em um subprocesso.
 *
 * <p>Funciona como um log de auditoria para as diversas etapas de análise (e.g., análise de cadastro,
 * análise de validação), registrando a ação, o analista, as observações e o resultado.
 */
@Entity
@Table(name = "ANALISE", schema = "sgc")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@SuppressWarnings("NullAway.Init")
public class Analise extends EntidadeBase {
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", length = 20, nullable = false)
    private TipoAnalise tipo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subprocesso_codigo", nullable = false)
    private Subprocesso subprocesso;

    @Enumerated(EnumType.STRING)
    @Column(name = "acao", length = 20, nullable = false)
    private TipoAcaoAnalise acao;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    @Column(name = "unidade_codigo", nullable = false)
    private Long unidadeCodigo;

    @Column(name = "usuario_titulo", length = 12, nullable = false)
    private String usuarioTitulo;

    @Column(name = "motivo", length = 200)
    private @Nullable String motivo;

    @Column(name = "observacoes", length = 500)
    private @Nullable String observacoes;
}
