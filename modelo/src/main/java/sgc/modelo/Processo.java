package sgc.modelo;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "PROCESSO")
public class Processo extends EntidadeBase {
    String descricao;

    @Enumerated(EnumType.STRING)
    TipoProcesso tipo;

    @Enumerated(EnumType.STRING)
    SituacaoProcesso situacao;

    LocalDateTime dataCriacao;
    LocalDateTime dataLimite;
    LocalDateTime dataFinalizacao;
}