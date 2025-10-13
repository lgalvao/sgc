package sgc.sgrh;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import sgc.comum.modelo.AdministradorRepo;
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

    public Collection<? extends GrantedAuthority> determineAuthorities(AdministradorRepo administradorRepo) {
        if (administradorRepo != null && administradorRepo.existsByUsuario(this)) {
            this.authorities = Stream.of(new SimpleGrantedAuthority("ROLE_ADMIN")).collect(Collectors.toList());
        } else if (unidade != null && this.equals(unidade.getTitular())) {
            switch (unidade.getTipo()) {
                case INTERMEDIARIA:
                    this.authorities = Stream.of(new SimpleGrantedAuthority("ROLE_GESTOR")).collect(Collectors.toList());
                    break;
                case OPERACIONAL:
                case INTEROPERACIONAL:
                    this.authorities = Stream.of(new SimpleGrantedAuthority("ROLE_CHEFE")).collect(Collectors.toList());
                    break;
                default:
                    this.authorities = Stream.of(new SimpleGrantedAuthority("ROLE_CHEFE")).collect(Collectors.toList());
                    break;
            }
        } else {
            this.authorities = Stream.of(new SimpleGrantedAuthority("ROLE_SERVIDOR")).collect(Collectors.toList());
        }
        return authorities;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (authorities == null) {
            // Esta chamada é para compatibilidade. A lógica principal está em determineAuthorities.
            return determineAuthorities(null);
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

}