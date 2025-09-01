package sgc.modelo;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Entity
@Table(name = "SUBPROCESSO")
public class Subprocesso extends EntidadeBase {
    @ManyToOne
    Processo processo;

    @ManyToOne
    Unidade unidade;

    @OneToOne
    Mapa mapa;

    @Enumerated(EnumType.STRING)
    SituacaoSubprocesso situacao;

    LocalDate dataLimiteEtapa1;
    LocalDate dataFimEtapa1;

    LocalDate dataLimiteEtapa2;
    LocalDate dataFimEtapa2;
}