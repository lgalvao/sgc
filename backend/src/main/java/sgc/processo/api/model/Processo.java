package sgc.processo.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sgc.comum.model.EntidadeBase;
import sgc.unidade.api.model.Unidade;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "PROCESSO", schema = "sgc")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@AttributeOverrides({@AttributeOverride(name = "codigo", column = @Column(name = "codigo"))})
public class Processo extends EntidadeBase {
    @Column(name = "data_criacao")
    private LocalDateTime dataCriacao;
    @Column(name = "data_finalizacao")
    private LocalDateTime dataFinalizacao;
    @Column(name = "data_limite")
    private LocalDateTime dataLimite;
    @Column(name = "descricao")
    private String descricao;
    @Enumerated(EnumType.STRING)
    @Column(name = "situacao", length = 20)
    private SituacaoProcesso situacao;
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", length = 20)
    private TipoProcesso tipo;
    @ManyToMany
    @JoinTable(
            name = "unidade_processo",
            schema = "sgc",
            joinColumns = @JoinColumn(name = "processo_codigo"),
            inverseJoinColumns = @JoinColumn(name = "unidade_codigo"))
    private Set<Unidade> participantes = new HashSet<>();

    public Processo(
            Long codigo,
            String descricao,
            TipoProcesso tipo,
            SituacaoProcesso situacao,
            LocalDateTime dataCriacao) {
        super(codigo);
        this.descricao = descricao;
        this.tipo = tipo;
        this.situacao = situacao;
        this.dataCriacao = dataCriacao;
    }

    public Processo(
            String descricao,
            TipoProcesso tipo,
            SituacaoProcesso situacao,
            LocalDateTime dataLimite) {
        super();
        this.descricao = descricao;
        this.tipo = tipo;
        this.situacao = situacao;
        this.dataLimite = dataLimite;
        this.dataCriacao = LocalDateTime.now();
    }
}
