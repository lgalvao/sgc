package sgc.subprocesso.modelo;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sgc.comum.modelo.EntidadeBase;
import sgc.unidade.modelo.Unidade;

import java.time.LocalDateTime;

@Entity
@Table(name = "MOVIMENTACAO", schema = "sgc")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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

    /**
     * Construtor de conveniência para registrar uma nova movimentação.
     * A data e hora são preenchidas automaticamente.
     */
    public Movimentacao(Subprocesso subprocesso, Unidade unidadeOrigem, Unidade unidadeDestino, String descricao) {
        super();
        this.subprocesso = subprocesso;
        this.unidadeOrigem = unidadeOrigem;
        this.unidadeDestino = unidadeDestino;
        this.descricao = descricao;
        this.dataHora = LocalDateTime.now();
    }

}