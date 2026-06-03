package sgc.processo.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.*;
import sgc.comum.model.*;
import sgc.organizacao.model.*;

import java.io.*;
import java.time.*;
import java.util.*;
import java.util.stream.*;

@Entity
@Table(name = "PROCESSO", schema = "sgc")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@AttributeOverride(name = "codigo", column = @Column(name = "codigo"))
@SuppressWarnings("NullAway.Init")
public class Processo extends EntidadeBase {
    @Serial
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
     */
    @OneToMany(mappedBy = "processo", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<UnidadeProcesso> participantes = new ArrayList<>();

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
        Set<Long> novosCodigos = novasUnidades.stream()
                .map(Unidade::getCodigo)
                .collect(Collectors.toSet());

        participantes.removeIf(up -> !novosCodigos.contains(up.getUnidadeCodigoPersistido()));

        adicionarParticipantes(novasUnidades);
    }

    public List<Long> getCodigosParticipantes() {
        return participantes.stream()
                .map(UnidadeProcesso::getUnidadeCodigoPersistido)
                .toList();
    }

    public String getSiglasParticipantes() {
        return participantes.stream()
                .map(UnidadeProcesso::getSigla)
                .filter(Objects::nonNull)
                .sorted()
                .collect(Collectors.joining(", "));
    }

    public Optional<UnidadeProcesso> buscarParticipante(Long unidadeCodigo) {
        return participantes.stream()
                .filter(participante -> Objects.equals(participante.getUnidadeCodigo(), unidadeCodigo))
                .findFirst();
    }
}
