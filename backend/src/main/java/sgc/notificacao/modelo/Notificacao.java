package sgc.notificacao.modelo;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sgc.comum.modelo.EntidadeBase;
import sgc.subprocesso.modelo.Subprocesso;
import sgc.unidade.modelo.Unidade;

import java.time.LocalDateTime;

@Entity
@Table(name = "NOTIFICACAO", schema = "sgc")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Notificacao extends EntidadeBase {
    @ManyToOne
    @JoinColumn(name = "subprocesso_codigo")
    private Subprocesso subprocesso;

    @Column(name = "data_hora")
    private LocalDateTime dataHora;

    @ManyToOne
    @JoinColumn(name = "unidade_origem_codigo")
    private Unidade unidadeOrigem;

    @ManyToOne
    @JoinColumn(name = "unidade_destino_codigo")
    private Unidade unidadeDestino;

    @Column(name = "conteudo", length = 500)
    private String conteudo;
}