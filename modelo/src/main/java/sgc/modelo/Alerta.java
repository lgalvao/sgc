package sgc.modelo;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "ALERTA")
public class Alerta extends EntidadeBase {
    @ManyToOne
    Unidade unidadeOrigem;

    @ManyToOne
    Unidade unidadeDestino;

    @ManyToOne
    Processo processo;

    String descricao;
    LocalDateTime dataHora;
    boolean lido;
}