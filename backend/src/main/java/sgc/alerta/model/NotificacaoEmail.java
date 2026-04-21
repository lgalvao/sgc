package sgc.alerta.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.*;
import org.jspecify.annotations.*;
import sgc.comum.model.*;
import sgc.subprocesso.model.*;

import java.time.*;

@Entity
@Table(name = "NOTIFICACAO_EMAIL", schema = "sgc")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@SuppressWarnings("NullAway.Init")
public class NotificacaoEmail extends EntidadeBase {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subprocesso_codigo")
    private @Nullable Subprocesso subprocesso;

    @Column(name = "tipo_transicao", length = 80)
    private @Nullable String tipoTransicao;

    @Column(name = "destinatario", nullable = false)
    private String destinatario;

    @Column(name = "assunto", nullable = false, length = 500)
    private String assunto;

    @Lob
    @Column(name = "corpo_html", nullable = false)
    private String corpoHtml;

    @Enumerated(EnumType.STRING)
    @Column(name = "situacao", nullable = false, length = 30)
    private SituacaoNotificacaoEmail situacao;

    @Column(name = "tentativas", nullable = false)
    private int tentativas;

    @Column(name = "proxima_tentativa_em")
    private @Nullable LocalDateTime proximaTentativaEm;

    @Column(name = "data_hora_criacao", nullable = false)
    private LocalDateTime dataHoraCriacao;

    @Column(name = "data_hora_envio")
    private @Nullable LocalDateTime dataHoraEnvio;

    @Column(name = "ultimo_erro", length = 2000)
    private @Nullable String ultimoErro;

    @Column(name = "chave_idempotencia", nullable = false, unique = true)
    private String chaveIdempotencia;
}
