package sesel.sgc.processo;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import sesel.sgc.EntidadeBase;
import sesel.sgc.historico.Historico;
import sesel.sgc.notificacao.Notificacao;
import sesel.sgc.processounidade.ProcessoUnidade;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "processo", uniqueConstraints = @UniqueConstraint(name = "uk_processo_historico", columnNames = {
        "historico_id"}))
@Getter
@Setter
public class Processo extends EntidadeBase {
    @NotBlank(message = "Nome do processo é obrigatório")
    @Column(nullable = false)
    private String nome;

    @NotNull(message = "Data limite é obrigatória")
    @Column(name = "datahora_limite")
    private LocalDate dataLimite;

    @Enumerated(EnumType.STRING)
    private ProcessoSituacao situacao = ProcessoSituacao.CRIADO;

    @OneToMany(mappedBy = "processo", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    private List<ProcessoUnidade> processosUnidades;

    @OneToMany(mappedBy = "processo", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Notificacao> notificacoesEnviadas = new ArrayList<>();

    @OneToOne
    @JoinColumn(name = "historico_id", foreignKey = @ForeignKey(name = "fk_processo_historico"))
    private Historico historico;

    private Set<UUID> unidadesParticipantes;

    public Processo() {
        this.processosUnidades = new ArrayList<>();
    }

    @Override
    public String toString() {
        return "Processo{nome='%s', dataLimite=%s, situacao=%s, unidadesParticipantes=%s}"
                .formatted(nome, dataLimite, situacao, unidadesParticipantes);
    }
}
