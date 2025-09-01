package sgc.modelo;

import jakarta.persistence.*;
import lombok.Getter;

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