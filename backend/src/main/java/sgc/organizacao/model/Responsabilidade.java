package sgc.organizacao.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Immutable;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Immutable
@Table(name = "VW_RESPONSABILIDADE", schema = "sgc")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Responsabilidade implements Serializable {

    @Id
    @Column(name = "unidade_codigo", nullable = false)
    private Long unidadeCodigo;

    @Column(name = "usuario_matricula", length = 8)
    private String usuarioMatricula;

    @Column(name = "usuario_titulo", length = 12, nullable = false)
    private String usuarioTitulo;

    @Column(name = "tipo", length = 30)
    private String tipo;

    @Column(name = "data_inicio")
    private LocalDateTime dataInicio;

    @Column(name = "data_fim")
    private LocalDateTime dataFim;

    @OneToOne
    @JoinColumn(name = "unidade_codigo", insertable = false, updatable = false)
    private Unidade unidade;

    @ManyToOne
    @JoinColumn(name = "usuario_titulo", referencedColumnName = "titulo", insertable = false, updatable = false)
    private Usuario usuario;
}
