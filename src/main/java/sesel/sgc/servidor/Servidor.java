package sesel.sgc.servidor;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import sesel.sgc.EntidadeBase;
import sesel.sgc.seguranca.Perfil;
import sesel.sgc.unidade.Unidade;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.util.stream.Collectors.toSet;

@Entity
@Table(name = "servidor", uniqueConstraints = {
        @UniqueConstraint(columnNames = "email", name = "uk_servidor_email")
})
@Getter
@Setter
@NoArgsConstructor
public class Servidor extends EntidadeBase implements UserDetails {
    @NotBlank(message = "Nome é obrigatório")
    @Column(nullable = false)
    private String nome;

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email deve ser válido")
    @Column(nullable = false, unique = true)
    private String email;

    @Column(length = 100, nullable = false)
    private String cargo = "";

    @NotNull(message = "A senha é obrigatória")
    @Column(nullable = false)
    private String senha;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "unidade_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_servidor_unidade")
    )
    private Unidade unidade;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "servidor_perfil",
            joinColumns = @JoinColumn(name = "servidor_id"),
            foreignKey = @ForeignKey(name = "fk_servidor_perfil_servidor"),
            uniqueConstraints = @UniqueConstraint(
                    name = "uk_servidor_perfil",
                    columnNames = {"servidor_id", "perfil"}
            )
    )
    @Column(name = "perfil", nullable = false)
    @Enumerated(EnumType.STRING)
    private List<Perfil> perfis = new ArrayList<>();

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return perfis.stream()
                .map(perfil -> new SimpleGrantedAuthority("ROLE_" + perfil.name()))
                .collect(toSet());
    }

    @Override
    public String getPassword() {
        return senha;
    }

    @Override
    public String getUsername() {
        return email;
    }
}
