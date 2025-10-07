package sgc.alerta;

import jakarta.persistence.*;
import lombok.*;
import sgc.comum.Usuario;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "ALERTA_USUARIO", schema = "sgc")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AlertaUsuario implements Serializable {
    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class Id implements Serializable {
        private Long alertaCodigo;
        private String usuarioTitulo;
    }

    @EmbeddedId
    private Id id;

    @MapsId("alertaCodigo")
    @ManyToOne
    @JoinColumn(name = "alerta_codigo", insertable = false, updatable = false)
    private Alerta alerta;

    @MapsId("usuarioTitulo")
    @ManyToOne
    @JoinColumn(name = "usuario_titulo", insertable = false, updatable = false)
    private Usuario usuario;

    @Column(name = "data_hora_leitura")
    private LocalDateTime dataHoraLeitura;
}