package sgc.organizacao.model;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import jakarta.persistence.Transient;
import lombok.*;
import lombok.experimental.*;
import org.hibernate.annotations.*;
import org.jspecify.annotations.*;
import org.springframework.security.core.*;
import org.springframework.security.core.userdetails.*;

import java.util.*;

@Entity
@Immutable
@Table(name = "VW_USUARIO", schema = "sgc")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class Usuario implements UserDetails {
    @Transient
    @JsonIgnore
    private @Nullable Set<GrantedAuthority> authorities;

    @Transient
    @JsonIgnore
    private Perfil perfilAtivo;

    @Transient
    @JsonIgnore
    private Long unidadeAtivaCodigo;

    @Id
    @JsonView(OrganizacaoViews.Publica.class)
    @Column(name = "titulo", length = 12, nullable = false)
    private String tituloEleitoral;

    @JsonView(OrganizacaoViews.Publica.class)
    @Column(name = "matricula", length = 8, nullable = false)
    private String matricula;

    @JsonView(OrganizacaoViews.Publica.class)
    @Column(name = "nome", nullable = false)
    private String nome;

    @JsonView(OrganizacaoViews.Publica.class)
    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "ramal", length = 20, nullable = false)
    @JsonIgnore
    private String ramal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_lot_codigo", nullable = false)
    @JsonIgnore
    private Unidade unidadeLotacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_comp_codigo")
    @JsonIgnore
    private Unidade unidadeCompetencia;

    @JsonView(OrganizacaoViews.Publica.class)
    @JsonProperty("unidadeCodigo")
    public Long getUnidadeCodigo() {
        return unidadeLotacao.getCodigo();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
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
        return authorities != null ? authorities : Set.of();
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
