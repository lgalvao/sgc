package sesel.sgc.unidade;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sesel.sgc.EntidadeBase;
import sesel.sgc.atribuicao.Atribuicao;
import sesel.sgc.servidor.Servidor;

import java.util.HashSet;
import java.util.Set;

/**
 * Representa uma unidade organizacional do sistema.
 * Pode ter uma hierarquia com unidade superiores e subordinadas.
 */
@Entity
@Table(name = "unidade",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "nome", name = "uk_unidade_nome")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class Unidade extends EntidadeBase {
    @NotBlank(message = "O nome da unidade é obrigatório")
    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private String descricao = "";

    @NotNull(message = "O tipo da unidade é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UnidadeTipo tipo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "unidade_superior_id",
            foreignKey = @ForeignKey(name = "fk_unidade_unidade_superior")
    )
    private Unidade unidadeSuperior;

    @OneToMany(mappedBy = "unidadeSuperior", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    private Set<Unidade> unidadesSubordinadas = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "titular_id",
            foreignKey = @ForeignKey(name = "fk_unidade_servidor_titular")
    )
    private Servidor titular;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "substituto_id",
            foreignKey = @ForeignKey(name = "fk_unidade_servidor_substituto")
    )
    private Servidor substituto;

    @OneToMany(mappedBy = "unidade", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    private Set<Servidor> servidores = new HashSet<>();

    @OneToMany(mappedBy = "unidade", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    private Set<Atribuicao> atribuicoesTemporarias = new HashSet<>();
}
