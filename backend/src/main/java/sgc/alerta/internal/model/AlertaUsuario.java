package sgc.alerta.internal.model;

import jakarta.persistence.*;
import lombok.*;
import sgc.sgrh.api.model.Usuario;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Tabela de associação que conecta um {@link Alerta} a um {@link Usuario} específico.
 *
 * <p>Esta entidade rastreia quais usuários receberam um alerta e quando o leram. Utiliza uma chave
 * primária composta ({@link Chave}) para identificar a relação.
 */
@Entity
@Table(name = "ALERTA_USUARIO", schema = "sgc")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AlertaUsuario implements Serializable {
    @EmbeddedId
    private Chave id;
    @MapsId("alertaCodigo")
    @ManyToOne
    @JoinColumn(name = "alerta_codigo", insertable = false, updatable = false)
    private Alerta alerta;
    @MapsId("usuarioTitulo")
    @ManyToOne
    @JoinColumn(
            name = "usuario_titulo",
            referencedColumnName = "titulo",
            insertable = false,
            updatable = false)
    private Usuario usuario;
    @Column(name = "data_hora_leitura")
    private LocalDateTime dataHoraLeitura;

    /**
     * Chave primária composta para a entidade {@link AlertaUsuario}.
     */
    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class Chave implements Serializable {
        private Long alertaCodigo;
        private String usuarioTitulo;
    }
}
