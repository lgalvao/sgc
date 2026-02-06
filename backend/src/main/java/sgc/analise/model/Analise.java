package sgc.analise.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import sgc.comum.model.EntidadeBase;
import sgc.subprocesso.model.Subprocesso;

import java.time.LocalDateTime;

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
public class Analise extends EntidadeBase {
    @ManyToOne
    @JoinColumn(name = "subprocesso_codigo", nullable = false)
    private Subprocesso subprocesso;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    @Column(name = "observacoes", length = 500)
    private String observacoes;

    @Enumerated(EnumType.STRING)
    @Column(name = "acao", length = 20, nullable = false)
    private TipoAcaoAnalise acao;

    @Column(name = "unidade_codigo")
    private Long unidadeCodigo;

    @Column(name = "usuario_titulo", length = 12)
    private String usuarioTitulo;

    @Column(name = "motivo", length = 200)
    private String motivo;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", length = 20, nullable = false)
    private TipoAnalise tipo;
}
