package sgc.modelo;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;

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