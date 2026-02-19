package sgc.processo.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import sgc.comum.model.EntidadeBase;
import sgc.organizacao.model.Unidade;

import java.io.Serial;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "PROCESSO", schema = "sgc")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@AttributeOverride(name = "codigo", column = @Column(name = "codigo"))
public class Processo extends EntidadeBase {
    @Serial
    private static final long serialVersionUID = 1L;
    
    @Builder.Default
    @Column(name = "data_criacao", nullable = false)
    @JsonView(ProcessoViews.Publica.class)
    private LocalDateTime dataCriacao = LocalDateTime.now();

    @Column(name = "data_finalizacao")
    @JsonView(ProcessoViews.Publica.class)
    private LocalDateTime dataFinalizacao;

    @Column(name = "data_limite", nullable = false)
    @JsonView(ProcessoViews.Publica.class)
    private LocalDateTime dataLimite;

    @Column(name = "descricao", nullable = false)
    @JsonView(ProcessoViews.Publica.class)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(name = "situacao", length = 20, nullable = false)
    @JsonView(ProcessoViews.Publica.class)
    private SituacaoProcesso situacao;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", length = 20, nullable = false)
    @JsonView(ProcessoViews.Publica.class)
    private TipoProcesso tipo;

    /**
     * Snapshots das unidades participantes.
     * Captura a hierarquia vigente no momento da inicialização do processo
     */
    @OneToMany(mappedBy = "processo", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<UnidadeProcesso> participantes = new ArrayList<>();

    @Override
    @JsonView(ProcessoViews.Publica.class)
    public Long getCodigo() {
        return super.getCodigo();
    }

    /**
     * Adiciona unidades participantes criando snapshots do estado atual.
     */
    public void adicionarParticipantes(Set<Unidade> unidades) {
        for (Unidade unidade : unidades) {
            boolean jaParticipa = participantes.stream()
                .anyMatch(up -> Objects.equals(up.getUnidadeCodigo(), unidade.getCodigo()));
            if (!jaParticipa) {
                UnidadeProcesso snapshot = UnidadeProcesso.criarSnapshot(this, unidade);
                participantes.add(snapshot);
            }
        }
    }

    /**
     * Sincroniza as unidades participantes, mantendo snapshosts existentes 
     * e adicionando novos apenas para unidades que ainda não participam.
     */
    public void sincronizarParticipantes(Set<Unidade> novasUnidades) {
        // 1. Remover quem não está mais na nova lista
        Set<Long> novosCodigos = novasUnidades.stream()
            .map(Unidade::getCodigo)
            .collect(Collectors.toSet());
            
        participantes.removeIf(up -> !novosCodigos.contains(up.getUnidadeCodigo()));
        
        // 2. Adicionar apenas quem é novo
        adicionarParticipantes(novasUnidades);
    }

    public List<Long> getCodigosParticipantes() {
        if (participantes == null) return List.of();
        return participantes.stream()
                .map(UnidadeProcesso::getUnidadeCodigo)
                .toList();
    }

    @JsonView(ProcessoViews.Publica.class)
    @JsonProperty("unidadesParticipantes")
    public String getSiglasParticipantes() {
        if (participantes == null) return null;
        return participantes.stream()
                .map(UnidadeProcesso::getSigla)
                .filter(Objects::nonNull)
                .sorted()
                .collect(Collectors.joining(", "));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Processo processo)) return false;
        return getCodigo() != null && getCodigo().equals(processo.getCodigo());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCodigo());
    }
}
