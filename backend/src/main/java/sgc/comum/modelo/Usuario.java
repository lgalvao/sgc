package sgc.comum.modelo;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
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
@AllArgsConstructor
public class Usuario implements Serializable, UserDetails {
    @Id
    @Column(name = "titulo", length = 20)
    private String titulo;

    @Column(name = "nome")
    private String nome;

    @Column(name = "email")
    private String email;

    @Column(name = "ramal", length = 20)
    private String ramal;

    @ManyToOne
    @JoinColumn(name = "unidade_codigo")
    private Unidade unidade;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Usuario usuario = (Usuario) o;
        return java.util.Objects.equals(titulo, usuario.titulo);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(titulo);
    }

    @Transient
    private Collection<? extends GrantedAuthority> authorities;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (authorities == null) {
            if ("admin".equalsIgnoreCase(titulo)) {
                this.authorities = Stream.of(new SimpleGrantedAuthority("ROLE_ADMIN")).collect(Collectors.toList());
            } else if (titulo != null && titulo.startsWith("gestor")) {
                this.authorities = Stream.of(new SimpleGrantedAuthority("ROLE_GESTOR")).collect(Collectors.toList());
            } else {
                this.authorities = Stream.of(new SimpleGrantedAuthority("ROLE_CHEFE")).collect(Collectors.toList());
            }
        }
        return authorities;
    }

    @Override
    public String getPassword() {
        return null; // Not used
    }

    @Override
    public String getUsername() {
        return titulo;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}