package sgc.configuracoes.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import sgc.comum.Mensagens;
import sgc.comum.model.EntidadeBase;
import sgc.configuracoes.ConfiguracaoRequest;

@Entity
@Table(name = "CONFIGURACAO", schema = "sgc")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@SuppressWarnings("NullAway.Init")
public class Configuracao extends EntidadeBase {
    @Column(name = "chave", length = 50, nullable = false)
    @NotBlank(message = Mensagens.CHAVE_OBRIGATORIA)
    @Size(max = 50, message = Mensagens.CHAVE_MAX)
    private String chave;

    @Column(name = "descricao")
    private String descricao;

    @Column(name = "valor", nullable = false)
    @NotBlank(message = Mensagens.VALOR_OBRIGATORIO)
    private String valor;

    public void atualizarDe(ConfiguracaoRequest request) {
        this.descricao = request.descricao();
        this.valor = request.valor();
    }
}
