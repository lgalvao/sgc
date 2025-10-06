package sgc.subprocesso;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Entity
@Table(name = "SITUACAO_SUBPROCESSO", schema = "sgc")
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