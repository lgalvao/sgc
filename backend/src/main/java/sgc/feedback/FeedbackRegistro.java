package sgc.feedback;

import jakarta.persistence.*;
import lombok.*;
import org.jspecify.annotations.*;

import java.time.*;
import java.util.*;

/**
 * Entidade que persiste um registro de feedback enviado durante homologação.
 */
@Entity
@Table(name = "FEEDBACK", schema = "sgc")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SuppressWarnings("NullAway.Init")
public class FeedbackRegistro {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private FeedbackTipo tipo;

    @Column(name = "nota", nullable = false, length = 2000)
    private String nota;

    @Lob
    @Column(name = "metadata_json")
    private @Nullable String metadataJson;

    @Column(name = "caminho_screenshot", length = 500)
    private @Nullable String caminhoScreenshot;

    @Column(name = "usuario_id", nullable = false, length = 100)
    private String usuarioId;

    @Column(name = "usuario_nome", nullable = false, length = 200)
    private String usuarioNome;

    @Column(name = "enviado_em", nullable = false)
    private OffsetDateTime enviadoEm;

    @Column(name = "rota", nullable = false, length = 500)
    private String rota;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private FeedbackStatus status;
}
