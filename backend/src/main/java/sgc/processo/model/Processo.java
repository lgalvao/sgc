package sgc.processo.model;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import sgc.comum.model.EntidadeBase;
import sgc.organizacao.model.Unidade;

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
    private static final long serialVersionUID = 1L;
    
    @Builder.Default
    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime dataCriacao = LocalDateTime.now();

    @Column(name = "data_finalizacao")
    private LocalDateTime dataFinalizacao;

    @Column(name = "data_limite", nullable = false)
    private LocalDateTime dataLimite;

    @Column(name = "descricao", nullable = false)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(name = "situacao", length = 20, nullable = false)
    private SituacaoProcesso situacao;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", length = 20, nullable = false)
    private TipoProcesso tipo;

    /**
     * Snapshots das unidades participantes.
     * Captura a hierarquia vigente no momento da inicialização do processo
     * conforme CDU-04 e CDU-05.
     */
    @OneToMany(mappedBy = "processo", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<UnidadeProcesso> participantes = new ArrayList<>();

    /**
     * Adiciona unidades participantes criando snapshots do estado atual.
     * 
     * @param unidades as unidades a serem adicionadas
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
     * 
     * @param novasUnidades o conjunto atualizado de unidades participantes
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

    /**
     * Retorna os códigos das unidades participantes.
     */
    public List<Long> getCodigosParticipantes() {
        return participantes.stream()
                .map(UnidadeProcesso::getUnidadeCodigo)
                .toList();
    }

    /**
     * Retorna as siglas das unidades participantes.
     */
    public String getSiglasParticipantes() {
        return participantes.stream()
                .map(UnidadeProcesso::getSigla)
                .filter(Objects::nonNull)
                .sorted()
                .collect(Collectors.joining(", "));
    }
}
