package sgc.modelo.base;

import jakarta.persistence.*;
import lombok.Getter;
import sgc.modelo.pessoas.Unidade;

import java.time.LocalDate;

@Getter
@Entity
@Table(name = "SUBPROCESSO")
public class Subprocesso extends EntidadeBase {
    @ManyToOne
    Processo processo;

    @ManyToOne
    Unidade unidade;

    @Enumerated(EnumType.STRING)
    SituacaoSubprocesso situacao;

    LocalDate dataLimiteEtapa1;
    LocalDate dataFimEtapa1;

    LocalDate dataLimiteEtapa2;
    LocalDate dataFimEtapa2;
}