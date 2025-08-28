package sgc.modelo.acomp;

import jakarta.persistence.*;
import lombok.Getter;
import sgc.modelo.pessoas.Unidade;
import sgc.modelo.base.EntidadeBase;
import sgc.modelo.base.Subprocesso;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "MOVIMENTACAO")
public class Movimentacao extends EntidadeBase {
    @ManyToOne
    Subprocesso subprocesso;

    @ManyToOne
    Unidade unidadeOrigem;

    @ManyToOne
    Unidade unidadeDestino;

    String descricao;
    LocalDateTime dataHora;
}