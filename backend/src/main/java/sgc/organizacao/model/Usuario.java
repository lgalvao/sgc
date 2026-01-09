package sgc.organizacao.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.LazyInitializationException;
import org.hibernate.annotations.Immutable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

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
@AllArgsConstructor
@Builder
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

    @Builder.Default
    @OneToMany(mappedBy = "usuario", fetch = FetchType.LAZY)
    private Set<AtribuicaoTemporaria> atribuicoesTemporarias = new java.util.HashSet<>();

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
        // Tenta carregar atribuições temporárias, mas tolera LazyInitializationException
        try {
            // Verifica se a coleção não é nula antes de iterar
            // A iteração irá tentar inicializar a coleção lazy, o que pode causar LazyInitializationException
            if (atribuicoesTemporarias != null) {
                for (AtribuicaoTemporaria temp : atribuicoesTemporarias) {
                    if ((temp.getDataInicio() == null || 
                        !temp.getDataInicio().isAfter(now))
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
        } catch (LazyInitializationException e) {
            // Se não há sessão disponível, apenas retorna as atribuições do cache
            // Isso é esperado quando o método é chamado fora de uma transação
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
}
