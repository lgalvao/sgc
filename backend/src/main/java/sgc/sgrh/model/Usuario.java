package sgc.sgrh.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Immutable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import sgc.unidade.model.AtribuicaoTemporaria;
import sgc.unidade.model.Unidade;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Immutable
@Table(name = "VW_USUARIO", schema = "sgc")
@Getter
@Setter
@NoArgsConstructor
public class Usuario implements UserDetails {
    @Id
    @Column(name = "titulo", length = 12)
    private String tituloEleitoral;

    @Column(name = "matricula", length = 8)
    private String matricula;

    @Column(name = "nome")
    private String nome;

    @Column(name = "email")
    private String email;

    @Column(name = "ramal", length = 20)
    private String ramal;

    @ManyToOne
    @JoinColumn(name = "unidade_lot_codigo")
    private Unidade unidadeLotacao;

    @ManyToOne
    @JoinColumn(name = "unidade_comp_codigo")
    private Unidade unidadeCompetencia;

    @OneToMany(mappedBy = "usuario", fetch = FetchType.EAGER)
    private Set<AtribuicaoTemporaria> atribuicoesTemporarias = new java.util.HashSet<>();

    public Usuario(
            String tituloEleitoral,
            String nome,
            String email,
            String ramal,
            Unidade unidadeLotacao) {
        this.tituloEleitoral = tituloEleitoral;
        this.matricula = null;
        this.nome = nome;
        this.email = email;
        this.ramal = ramal;
        this.unidadeLotacao = unidadeLotacao;
        this.unidadeCompetencia = null;
    }

    @Transient
    private Set<UsuarioPerfil> atribuicoesCache;

    public void setAtribuicoes(Set<UsuarioPerfil> atribuicoes) {
        this.atribuicoesCache = atribuicoes;
    }

    public Set<UsuarioPerfil> getAtribuicoes() {
        return atribuicoesCache != null ? atribuicoesCache : new java.util.HashSet<>();
    }

    public Set<UsuarioPerfil> getTodasAtribuicoes() {
        Set<UsuarioPerfil> todas = new java.util.HashSet<>();
        if (atribuicoesCache != null) {
            todas.addAll(atribuicoesCache);
        }
        LocalDateTime now = LocalDateTime.now();
        if (atribuicoesTemporarias != null) {
            for (AtribuicaoTemporaria temp : atribuicoesTemporarias) {
                if ((temp.getDataInicio() == null || !temp.getDataInicio().isAfter(now))
                        && (temp.getDataTermino() == null
                                || !temp.getDataTermino().isBefore(now))) {
                    UsuarioPerfil perfil = new UsuarioPerfil();
                    perfil.setUsuarioTitulo(this.tituloEleitoral);
                    perfil.setUsuario(this);
                    perfil.setUnidadeCodigo(temp.getUnidade().getCodigo());
                    perfil.setUnidade(temp.getUnidade());
                    perfil.setPerfil(temp.getPerfil());
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

    @Override
    public String toString() {
        return "Usuario{titulo='%s', nome='%s', unidade=%s}"
                .formatted(tituloEleitoral, nome, unidadeLotacao.getSigla());
    }
}
