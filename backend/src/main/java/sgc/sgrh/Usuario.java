package sgc.sgrh;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import sgc.unidade.modelo.Unidade;

import java.io.Serializable;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Entity
@Table(name = "USUARIO", schema = "sgc")
@Getter
@Setter
@NoArgsConstructor
public class Usuario implements Serializable, UserDetails {
    @Id
    @Column(name = "titulo_eleitoral")
    private Long tituloEleitoral;

    @Column(name = "nome")
    private String nome;

    @Column(name = "email")
    private String email;

    @Column(name = "ramal", length = 20)
    private String ramal;

    @ManyToOne
    @JoinColumn(name = "unidade_codigo")
    private Unidade unidade;

    @ElementCollection(targetClass = Perfil.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "USUARIO_PERFIL", joinColumns = @JoinColumn(name = "usuario_titulo_eleitoral"), schema = "sgc")
    @Enumerated(EnumType.STRING)
    @Column(name = "perfil")
    private java.util.Set<Perfil> perfis = new java.util.HashSet<>();


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Usuario usuario = (Usuario) o;
        return java.util.Objects.equals(tituloEleitoral, usuario.tituloEleitoral);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(tituloEleitoral);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return perfis.stream()
                .map(Perfil::toGrantedAuthority)
                .collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return null; // Not used
    }

    @Override
    public String getUsername() {
        return String.valueOf(tituloEleitoral);
    }

    public Usuario(Long tituloEleitoral, String nome, String email, String ramal, Unidade unidade, Collection<Perfil> perfis) {
        this.tituloEleitoral = tituloEleitoral;
        this.nome = nome;
        this.email = email;
        this.ramal = ramal;
        this.unidade = new Unidade(unidade);
        this.perfis = new java.util.HashSet<>(perfis);
    }

    public void setUnidade(Unidade unidade) {
        this.unidade = new Unidade(unidade);
    }

    public Unidade getUnidade() {
        return new Unidade(this.unidade);
    }
}