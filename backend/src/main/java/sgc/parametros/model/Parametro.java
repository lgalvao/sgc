package sgc.parametros.model;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.*;
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
