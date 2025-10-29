package sgc.processo.modelo;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sgc.comum.modelo.EntidadeBase;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import sgc.unidade.modelo.Unidade;

@Entity
@Table(name = "PROCESSO", schema = "sgc")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Processo extends EntidadeBase {
    public Processo(String descricao, TipoProcesso tipo, SituacaoProcesso situacao, LocalDateTime dataLimite) {
        super();
        this.descricao = descricao;
        this.tipo = tipo;
        this.situacao = situacao;
        this.dataLimite = dataLimite;
        this.dataCriacao = LocalDateTime.now();
    }

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
        name = "PROCESSO_UNIDADE",
        joinColumns = @JoinColumn(name = "processo_codigo"),
        inverseJoinColumns = @JoinColumn(name = "unidade_codigo")
    )
    private Set<Unidade> unidades = new HashSet<>();
}
