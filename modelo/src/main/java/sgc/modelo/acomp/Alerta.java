package sgc.modelo.acomp;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.*;
import sgc.modelo.pessoas.Unidade;
import sgc.modelo.base.EntidadeBase;
import sgc.modelo.base.Processo;

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