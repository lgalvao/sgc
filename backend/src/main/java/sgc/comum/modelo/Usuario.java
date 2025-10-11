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
    @Column(name = "titulo", length = 12)
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

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // For simplicity, we'll assign roles based on the username.
        if ("admin".equalsIgnoreCase(titulo)) {
            return Stream.of(new SimpleGrantedAuthority("ROLE_ADMIN")).collect(Collectors.toList());
        }
        return Stream.of(new SimpleGrantedAuthority("ROLE_CHEFE")).collect(Collectors.toList());
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