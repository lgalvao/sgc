package sgc.alerta.model;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.jspecify.annotations.Nullable;
import sgc.organizacao.model.Usuario;

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
@SuperBuilder
@SuppressWarnings("NullAway.Init")
public class AlertaUsuario implements Serializable {
    @EmbeddedId
    private Chave codigo;

    @MapsId("alertaCodigo")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alerta_codigo", insertable = false, updatable = false)
    private Alerta alerta;

    @MapsId("usuarioTitulo")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "usuario_titulo",
            referencedColumnName = "titulo",
            insertable = false,
            updatable = false)
    private Usuario usuario;

    @Column(name = "data_hora_leitura")
    private @Nullable LocalDateTime dataHoraLeitura;

    /**
     * Chave primária composta para a entidade {@link AlertaUsuario}.
     */
    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @SuperBuilder
    @EqualsAndHashCode
    @SuppressWarnings("NullAway.Init")
    public static class Chave implements Serializable {
        private Long alertaCodigo;
        private String usuarioTitulo;
    }
}
