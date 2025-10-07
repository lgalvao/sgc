package sgc.alerta;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sgc.comum.EntidadeBase;
import sgc.comum.Usuario;
import sgc.processo.Processo;
import sgc.unidade.Unidade;

import java.time.LocalDateTime;

@Entity
@Table(name = "ALERTA", schema = "sgc")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Alerta extends EntidadeBase {
    @ManyToOne
    @JoinColumn(name = "processo_codigo")
    private Processo processo;

    @Column(name = "data_hora")
    private LocalDateTime dataHora;

    @ManyToOne
    @JoinColumn(name = "unidade_origem_codigo")
    private Unidade unidadeOrigem;

    @ManyToOne
    @JoinColumn(name = "unidade_destino_codigo")
    private Unidade unidadeDestino;

    @ManyToOne
    @JoinColumn(name = "usuario_destino_titulo")
    private Usuario usuarioDestino;

    @Column(name = "descricao")
    private String descricao;
}