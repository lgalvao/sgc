package sgc.modelo;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "NOTIFICACAO")
public class Notificacao extends EntidadeBase {
    @ManyToOne
    Subprocesso subprocesso;

    @ManyToOne
    Unidade unidadeOrigem;

    @ManyToOne
    Unidade unidadeDestino;

    String conteudo;
    LocalDateTime dataHora;
}