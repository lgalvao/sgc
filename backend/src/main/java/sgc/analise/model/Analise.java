package sgc.analise.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import sgc.comum.model.EntidadeBase;
import sgc.subprocesso.model.Subprocesso;

/**
 * Representa um registro de análise realizado em um subprocesso.
 *
 * <p>Esta entidade funciona como um log de auditoria para as diversas etapas de análise (e.g.,
 * análise de cadastro, análise de validação), registrando a ação, o analista, as observações e o
 * resultado.
 */
@Entity
@Table(name = "ANALISE", schema = "sgc")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Analise extends EntidadeBase {
    @ManyToOne
    @JoinColumn(name = "subprocesso_codigo")
    private Subprocesso subprocesso;

    @Column(name = "data_hora")
    private LocalDateTime dataHora;

    @Column(name = "observacoes", length = 500)
    private String observacoes;

    @Enumerated(EnumType.STRING)
    @Column(name = "acao", length = 20)
    private TipoAcaoAnalise acao;

    @Column(name = "unidade_sigla", length = 30)
    private String unidadeSigla;

    @Column(name = "analista_usuario_titulo", length = 50)
    private String analistaUsuarioTitulo;

    @Column(name = "motivo", length = 500)
    private String motivo;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", length = 20)
    private TipoAnalise tipo;
}
