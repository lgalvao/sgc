package sgc.parametros.model;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import sgc.comum.model.EntidadeBase;

@Entity
@Table(name = "PARAMETRO", schema = "sgc")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class Parametro extends EntidadeBase {
    @JsonView(ConfiguracaoViews.Publica.class)
    @Column(name = "chave", length = 50, nullable = false)
    @NotBlank(message = "A chave não pode estar vazia")
    @Size(max = 50, message = "A chave deve ter no máximo 50 caracteres")
    private String chave;

    @JsonView(ConfiguracaoViews.Publica.class)
    @Column(name = "descricao")
    private String descricao;

    @JsonView(ConfiguracaoViews.Publica.class)
    @Column(name = "valor", nullable = false)
    @NotBlank(message = "O valor não pode estar vazio")
    private String valor;
}
