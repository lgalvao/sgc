package sgc.subprocesso.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sgc.comum.model.EntidadeBase;
import sgc.sgrh.model.Usuario;
import sgc.unidade.model.Unidade;

import java.time.LocalDateTime;

@Entity
@Table(name = "MOVIMENTACAO", schema = "sgc")
@Getter
@Setter
@NoArgsConstructor
public class Movimentacao extends EntidadeBase {
    @ManyToOne
    @JoinColumn(name = "subprocesso_codigo")
    private Subprocesso subprocesso;

    @Column(name = "data_hora")
    private LocalDateTime dataHora;

    @ManyToOne
    @JoinColumn(name = "unidade_origem_codigo")
    private Unidade unidadeOrigem;

    @ManyToOne
    @JoinColumn(name = "unidade_destino_codigo")
    private Unidade unidadeDestino;

    @Column(name = "descricao")
    private String descricao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_titulo")
    private Usuario usuario;

    public Movimentacao(
            Long codigo,
            Subprocesso subprocesso,
            Usuario usuario,
            String descricao,
            LocalDateTime dataHora) {

        super(codigo);
        this.subprocesso = subprocesso;
        this.usuario = usuario;
        this.descricao = descricao;
        this.dataHora = dataHora;
    }

    /**
     * Construtor de conveniência para registrar uma nova movimentação. A data e hora são
     * preenchidas automaticamente.
     */
    public Movimentacao(
            Subprocesso subprocesso,
            Unidade unidadeOrigem,
            Unidade unidadeDestino,
            String descricao,
            Usuario usuario) {
        super();
        this.subprocesso = subprocesso;
        this.unidadeOrigem = unidadeOrigem;
        this.unidadeDestino = unidadeDestino;
        this.descricao = descricao;
        this.usuario = usuario;
        this.dataHora = LocalDateTime.now();
    }
}
