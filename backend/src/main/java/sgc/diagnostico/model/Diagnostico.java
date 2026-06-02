package sgc.diagnostico.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.*;
import sgc.comum.model.*;
import sgc.subprocesso.model.*;

import java.time.*;
import java.util.*;

@Entity
@Table(name = "DIAGNOSTICO", schema = "sgc")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@AttributeOverride(name = "codigo", column = @Column(name = "codigo"))
public class Diagnostico extends EntidadeBase {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subprocesso_codigo", nullable = false, unique = true)
    private Subprocesso subprocesso;

    @Enumerated(EnumType.STRING)
    @Column(name = "situacao", length = 50, nullable = false)
    private SituacaoDiagnostico situacao;

    @Column(name = "data_conclusao")
    private LocalDateTime dataConclusao;

    @Column(name = "justificativa_conclusao")
    private String justificativaConclusao;

    @OneToMany(mappedBy = "diagnostico", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AvaliacaoServidor> avaliacaoServidores = new ArrayList<>();

    @OneToMany(mappedBy = "diagnostico", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OcupacaoCritica> ocupacaoCriticas = new ArrayList<>();
}
