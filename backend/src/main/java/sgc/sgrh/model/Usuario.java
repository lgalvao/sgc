package sgc.sgrh.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import sgc.unidade.model.AtribuicaoTemporaria;
import sgc.unidade.model.Unidade;

@Entity
@Table(name = "USUARIO", schema = "sgc")
@Getter
@Setter
@NoArgsConstructor
public class Usuario implements UserDetails {
    @Id
    @Column(name = "titulo_eleitoral")
    private String tituloEleitoral;

    @Column(name = "nome")
    private String nome;

    @Column(name = "email")
    private String email;

    @Column(name = "ramal", length = 20)
    private String ramal;

    @ManyToOne
    @JoinColumn(name = "unidade_codigo")
    private Unidade unidadeLotacao;

    @OneToMany(
            mappedBy = "usuario",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.EAGER)
    private Set<UsuarioPerfil> atribuicoes = new java.util.HashSet<>();

    @OneToMany(mappedBy = "usuario", fetch = FetchType.EAGER)
    private Set<AtribuicaoTemporaria> atribuicoesTemporarias = new java.util.HashSet<>();

    public Set<UsuarioPerfil> getTodasAtribuicoes() {
        Set<UsuarioPerfil> todas = new java.util.HashSet<>(atribuicoes);
        LocalDateTime now = LocalDateTime.now();
        if (atribuicoesTemporarias != null) {
            for (AtribuicaoTemporaria temp : atribuicoesTemporarias) {
                if ((temp.getDataInicio() == null || !temp.getDataInicio().isAfter(now))
                        && (temp.getDataTermino() == null
                                || !temp.getDataTermino().isBefore(now))) {
                    todas.add(new UsuarioPerfil(null, this, temp.getUnidade(), temp.getPerfil()));
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
        return getTodasAtribuicoes().stream()
                .map(UsuarioPerfil::getPerfil)
                .map(Perfil::toGrantedAuthority)
                .collect(Collectors.toSet());
    }

    @Override
    public String getPassword() {
        return null; // Not used
    }

    @Override
    public String getUsername() {
        return tituloEleitoral;
    }

    public Usuario(
            String tituloEleitoral,
            String nome,
            String email,
            String ramal,
            Unidade unidadeLotacao) {
        this.tituloEleitoral = tituloEleitoral;
        this.nome = nome;
        this.email = email;
        this.ramal = ramal;
        this.unidadeLotacao = unidadeLotacao;
    }

    @Override
    public String toString() {
        return "Usuario{titulo='%s', nome='%s', unidade=%s}"
                .formatted(tituloEleitoral, nome, unidadeLotacao.getSigla());
    }
}
