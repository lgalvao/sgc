package sgc.model;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "SUBPROCESSO", schema = "sgc")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Subprocesso implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "codigo")
    private Long codigo;

    @ManyToOne
    @JoinColumn(name = "processo_codigo")
    private Processo processo;

    @ManyToOne
    @JoinColumn(name = "unidade_codigo")
    private Unidade unidade;

    @ManyToOne
    @JoinColumn(name = "mapa_codigo")
    private Mapa mapa;

    @Column(name = "data_limite_etapa1")
    private LocalDate dataLimiteEtapa1;

    @Column(name = "data_fim_etapa1")
    private LocalDateTime dataFimEtapa1;

    @Column(name = "data_limite_etapa2")
    private LocalDate dataLimiteEtapa2;

    @Column(name = "data_fim_etapa2")
    private LocalDateTime dataFimEtapa2;

    @Column(name = "situacao_id", length = 50)
    private String situacaoId;
}