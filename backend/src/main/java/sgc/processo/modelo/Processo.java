package sgc.processo.modelo;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sgc.processo.SituacaoProcesso;
import sgc.comum.modelo.EntidadeBase;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "PROCESSO", schema = "sgc")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Processo extends EntidadeBase {

    public Processo(String descricao, TipoProcesso tipo, SituacaoProcesso situacao, LocalDate dataLimite) {
        super();
        this.descricao = descricao;
        this.tipo = tipo;
        this.situacao = situacao;
        this.dataLimite = dataLimite;
        this.dataCriacao = LocalDateTime.now();
    }

    /**
     * Construtor de cópia.
     */
    public Processo(Processo outro) {
        super(outro);
        if (outro == null) {
            return;
        }
        this.descricao = outro.descricao;
        this.tipo = outro.tipo;
        this.situacao = outro.situacao;
        this.dataLimite = outro.dataLimite;
        this.dataCriacao = outro.dataCriacao;
        this.dataFinalizacao = outro.dataFinalizacao;
    }
    @Column(name = "data_criacao")
    private LocalDateTime dataCriacao;

    @Column(name = "data_finalizacao")
    private LocalDateTime dataFinalizacao;

    @Column(name = "data_limite")
    private LocalDate dataLimite;

    @Column(name = "descricao")
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(name = "situacao", length = 20)
    private SituacaoProcesso situacao;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", length = 20)
    private TipoProcesso tipo;
}
