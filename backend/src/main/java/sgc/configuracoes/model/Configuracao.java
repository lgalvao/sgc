package sgc.configuracoes.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.*;
import sgc.comum.*;
import sgc.comum.model.*;
import sgc.configuracoes.*;

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
