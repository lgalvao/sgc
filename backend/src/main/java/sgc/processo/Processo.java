package sgc.processo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sgc.comum.EntidadeBase;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "PROCESSO", schema = "sgc")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Processo extends EntidadeBase {
    @Column(name = "data_criacao")
    private LocalDateTime dataCriacao;

    @Column(name = "data_finalizacao")
    private LocalDateTime dataFinalizacao;

    @Column(name = "data_limite")
    private LocalDate dataLimite;

    @Column(name = "descricao")
    private String descricao;

    @Column(name = "situacao", length = 20)
    private String situacao;

    @Column(name = "tipo", length = 20)
    private String tipo;
}