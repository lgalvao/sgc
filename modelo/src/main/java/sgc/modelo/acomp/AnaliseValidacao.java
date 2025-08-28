package sgc.modelo.acomp;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import sgc.modelo.base.EntidadeBase;
import sgc.modelo.base.Subprocesso;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "ANALISE_VALIDACAO")
public class AnaliseValidacao extends EntidadeBase {
    @ManyToOne
    Subprocesso subprocesso;

    LocalDateTime dataHora;
    String observacoes;
}