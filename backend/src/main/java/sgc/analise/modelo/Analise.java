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
@Table(name = "ANALISE", schema = "sgc")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
/**
 * Representa um registro de análise realizado em um subprocesso.
 * <p>
 * Esta entidade funciona como um log de auditoria para as diversas etapas de
 * análise (e.g., análise de cadastro, análise de validação), registrando a ação,
 * o analista, as observações e o resultado.
 */
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
