package sgc.processo.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.*;
import sgc.comum.model.EntidadeBase;
import sgc.organizacao.model.Usuario;

@Entity
@Table(name = "SERVIDOR_PROCESSO", schema = "sgc")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@AttributeOverride(name = "codigo", column = @Column(name = "codigo"))
public class ServidorProcesso extends EntidadeBase {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processo_codigo", nullable = false)
    private Processo processo;

    @Column(name = "unidade_codigo", nullable = false)
    private Long unidadeCodigo;

    @Column(name = "usuario_titulo", length = 12, nullable = false)
    private String usuarioTitulo;

    @Column(name = "matricula", length = 8)
    private String matricula;

    @Column(name = "nome", nullable = false)
    private String nome;

    @Column(name = "email", nullable = false)
    private String email;

    public static ServidorProcesso criarSnapshot(Processo processo, Long unidadeCodigo, Usuario usuario) {
        return ServidorProcesso.builder()
                .processo(processo)
                .unidadeCodigo(unidadeCodigo)
                .usuarioTitulo(usuario.getTituloEleitoral())
                .matricula(usuario.getMatricula())
                .nome(usuario.getNome())
                .email(usuario.getEmail())
                .build();
    }
}
