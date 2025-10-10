package sgc.analise.modelo;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sgc.analise.enums.TipoAcaoAnalise;
import sgc.comum.modelo.EntidadeBase;
import sgc.subprocesso.modelo.Subprocesso;

import java.time.LocalDateTime;

@Entity
@Table(name = "ANALISE_CADASTRO", schema = "sgc")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AnaliseCadastro extends EntidadeBase {
    @ManyToOne
    @JoinColumn(name = "subprocesso_codigo")
    private Subprocesso subprocesso;

    @Column(name = "data_hora")
    private LocalDateTime dataHora;

    @Column(name = "analista_usuario_titulo", length = 50)
    private String analistaUsuarioTitulo;

    @Column(name = "unidade_sigla", length = 20)
    private String unidadeSigla;

    @Enumerated(EnumType.STRING)
    @Column(name = "acao", length = 20)
    private TipoAcaoAnalise acao;

    @Column(name = "motivo", length = 500)
    private String motivo;

    @Column(name = "observacoes", length = 500)
    private String observacoes;
}