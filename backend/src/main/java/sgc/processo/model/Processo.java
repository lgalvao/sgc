package sgc.processo.model;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.BatchSize;
import sgc.comum.model.EntidadeBase;
import sgc.organizacao.model.Unidade;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "PROCESSO", schema = "sgc")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@AttributeOverride(name = "codigo", column = @Column(name = "codigo"))
public class Processo extends EntidadeBase {
    @Builder.Default
    @Column(name = "data_criacao")
    private LocalDateTime dataCriacao = LocalDateTime.now();

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
    @BatchSize(size = 50)
    @Builder.Default
    private Set<Unidade> participantes = new HashSet<>();

    public Set<Unidade> getParticipantes() {
        return participantes;
    }
}
