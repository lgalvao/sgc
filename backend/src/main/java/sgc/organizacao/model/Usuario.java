package sgc.organizacao.model;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Immutable;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Immutable
@Table(name = "VW_USUARIO", schema = "sgc")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class Usuario implements UserDetails {
    @Id
    @Column(name = "titulo", length = 12, nullable = false)
    private String tituloEleitoral;

    @Column(name = "matricula", length = 8, nullable = false)
    private String matricula;

    @Column(name = "nome", nullable = false)
    private String nome;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "ramal", length = 20, nullable = false)
    private String ramal;

    @ManyToOne
    @JoinColumn(name = "unidade_lot_codigo", nullable = false)
    private Unidade unidadeLotacao;

    @ManyToOne
    @JoinColumn(name = "unidade_comp_codigo")
    private Unidade unidadeCompetencia;

    @Builder.Default
    @OneToMany(mappedBy = "usuario", fetch = FetchType.LAZY)
    private Set<AtribuicaoTemporaria> atribuicoesTemporarias = new HashSet<>();

    /**
     * Retorna todas as atribuições do usuário (permanentes + temporárias ativas).
     * 
     * @param atribuicoesPermanentes As atribuições permanentes do usuário (devem ser carregadas externamente)
     * @return Conjunto de todas as atribuições (permanentes + temporárias ativas)
     */
    public Set<UsuarioPerfil> getTodasAtribuicoes(Set<UsuarioPerfil> atribuicoesPermanentes) {
        Set<UsuarioPerfil> todas = new HashSet<>(atribuicoesPermanentes != null ? atribuicoesPermanentes : Set.of());

        LocalDateTime now = LocalDateTime.now();
        if (atribuicoesTemporarias != null) {
            for (AtribuicaoTemporaria temp : atribuicoesTemporarias) {
                if (!temp.getDataInicio().isAfter(now) && !temp.getDataTermino().isBefore(now)) {
                    UsuarioPerfil perfil = new UsuarioPerfil()
                            .setUsuarioTitulo(this.tituloEleitoral)
                            .setUsuario(this)
                            .setUnidadeCodigo(temp.getUnidade().getCodigo())
                            .setUnidade(temp.getUnidade())
                            .setPerfil(temp.getPerfil());

                    todas.add(perfil);
                }
            }
        }
        return todas;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Usuario usuario = (Usuario) obj;
        return Objects.equals(tituloEleitoral, usuario.tituloEleitoral);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tituloEleitoral);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Nota: Este método não pode carregar atribuições permanentes aqui
        // pois não temos acesso ao repositório. Retorna vazio.
        // As atribuições devem ser gerenciadas pelo UserDetailsService.
        return Set.of();
    }

    @Override
    @Nullable
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return tituloEleitoral;
    }
}
