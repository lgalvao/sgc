package sesel.sgc.notificacao;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import sesel.sgc.EntidadeBase;
import sesel.sgc.processo.Processo;
import sesel.sgc.unidade.Unidade;

import java.time.LocalDateTime;

@Entity
@Table(name = "notificacao")
@Getter
@Setter
public class Notificacao extends EntidadeBase {
    @Enumerated(EnumType.STRING)
    private NotificacaoTipo tipo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "unidade_id",
            foreignKey = @ForeignKey(name = "fk_notificacao_unidade")
    )
    private Unidade unidade;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "processo_id",
            foreignKey = @ForeignKey(name = "fk_notificacao_processo")
    )
    private Processo processo;

    @NotBlank(message = "Assunto é obrigatório")
    @Column(nullable = false)
    private String assunto;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String conteudo;

    @Column(name = "datahora_envio")
    private LocalDateTime dataHoraEnvio;
}
