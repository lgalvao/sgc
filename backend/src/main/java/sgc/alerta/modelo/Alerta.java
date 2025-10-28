package sgc.alerta.modelo;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sgc.comum.modelo.EntidadeBase;
import sgc.processo.modelo.Processo;
import sgc.sgrh.modelo.Usuario;
import sgc.unidade.modelo.Unidade;

import java.time.LocalDateTime;

/**
 * Representa um alerta ou notificação dentro do sistema.
 * <p>
 * Alertas são gerados em resposta a eventos importantes no sistema,
 * como o início de um processo, e são direcionados a unidades ou usuários específicos.
 */
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