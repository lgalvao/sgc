package sgc.model;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "SITUACAO_SUBPROCESSO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SituacaoSubprocesso implements Serializable {

    @Id
    @Column(name = "id", length = 50)
    private String id;

    @Column(name = "descricao", length = 50)
    private String descricao;
}