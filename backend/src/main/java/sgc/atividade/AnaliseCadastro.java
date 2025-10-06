package sgc.atividade;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sgc.comum.BaseEntity;
import sgc.subprocesso.Subprocesso;

import java.time.LocalDateTime;

@Entity
@Table(name = "ANALISE_CADASTRO", schema = "sgc")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AnaliseCadastro extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "subprocesso_codigo")
    private Subprocesso subprocesso;

    @Column(name = "data_hora")
    private LocalDateTime dataHora;

    @Column(name = "analista_usuario_titulo", length = 50)
    private String analistaUsuarioTitulo;

    @Column(name = "unidade_sigla", length = 20)
    private String unidadeSigla;

    @Column(name = "acao", length = 20)
    private String acao;

    @Column(name = "motivo", length = 500)
    private String motivo;

    @Column(name = "observacoes", length = 500)
    private String observacoes;
}