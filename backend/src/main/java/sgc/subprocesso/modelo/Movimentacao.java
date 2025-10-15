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
        this.subprocesso = new Subprocesso(subprocesso);
        this.unidadeOrigem = new Unidade(unidadeOrigem);
        this.unidadeDestino = new Unidade(unidadeDestino);
        this.descricao = descricao;
        this.dataHora = LocalDateTime.now();
    }

    public void setSubprocesso(Subprocesso subprocesso) {
        this.subprocesso = new Subprocesso(subprocesso);
    }

    public Subprocesso getSubprocesso() {
        return new Subprocesso(this.subprocesso);
    }

    public void setUnidadeOrigem(Unidade unidadeOrigem) {
        this.unidadeOrigem = new Unidade(unidadeOrigem);
    }

    public Unidade getUnidadeOrigem() {
        return new Unidade(this.unidadeOrigem);
    }

    public void setUnidadeDestino(Unidade unidadeDestino) {
        this.unidadeDestino = new Unidade(unidadeDestino);
    }

    public Unidade getUnidadeDestino() {
        return new Unidade(this.unidadeDestino);
    }
}