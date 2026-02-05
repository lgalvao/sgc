package sgc.notificacao.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import sgc.comum.model.EntidadeBase;
import sgc.organizacao.model.Unidade;
import sgc.subprocesso.model.Subprocesso;

import java.time.LocalDateTime;

@Entity
@Table(name = "NOTIFICACAO", schema = "sgc")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class Notificacao extends EntidadeBase {
    @ManyToOne
    @JoinColumn(name = "subprocesso_codigo", nullable = false)
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
