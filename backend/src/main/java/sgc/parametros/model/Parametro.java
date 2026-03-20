package sgc.parametros.model;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.*;
import sgc.comum.*;
import sgc.comum.model.*;

@Entity
@Table(name = "PARAMETRO", schema = "sgc")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class Parametro extends EntidadeBase {
    @JsonView(ConfiguracaoViews.Publica.class)
    @Column(name = "chave", length = 50, nullable = false)
    @NotBlank(message = SgcMensagens.CHAVE_OBRIGATORIA)
    @Size(max = 50, message = SgcMensagens.CHAVE_MAX)
    private String chave;

    @JsonView(ConfiguracaoViews.Publica.class)
    @Column(name = "descricao")
    private String descricao;

    @JsonView(ConfiguracaoViews.Publica.class)
    @Column(name = "valor", nullable = false)
    @NotBlank(message = SgcMensagens.VALOR_OBRIGATORIO)
    private String valor;

    public void atualizarDe(sgc.parametros.ParametroRequest request) {
        this.descricao = request.descricao();
        this.valor = request.valor();
    }
}
