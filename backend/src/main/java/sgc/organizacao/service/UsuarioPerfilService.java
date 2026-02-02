package sgc.organizacao.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import sgc.organizacao.model.Usuario;
import sgc.organizacao.model.UsuarioPerfil;
import sgc.organizacao.model.UsuarioPerfilRepo;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsuarioPerfilService {
    private final UsuarioPerfilRepo usuarioPerfilRepo;

    public List<UsuarioPerfil> buscarPorUsuario(String usuarioTitulo) {
        return usuarioPerfilRepo.findByUsuarioTitulo(usuarioTitulo);
    }

    public void carregarAuthorities(Usuario usuario) {
        Set<UsuarioPerfil> permanentes = new HashSet<>(usuarioPerfilRepo.findByUsuarioTitulo(usuario.getTituloEleitoral()));
        Set<UsuarioPerfil> todas = usuario.getTodasAtribuicoes(permanentes);

        Set<GrantedAuthority> authorities = todas.stream()
                .map(a -> a.getPerfil().toGrantedAuthority())
                .collect(Collectors.toSet());

        usuario.setAuthorities(authorities);
    }

    /**
     * Helper para carregar atribuições de uma lista de usuários de forma otimizada
     * (embora a implementação atual ainda faça N queries, centraliza a lógica)
     */
    public Set<UsuarioPerfil> buscarAtribuicoesParaCache(String usuarioTitulo) {
        return new HashSet<>(usuarioPerfilRepo.findByUsuarioTitulo(usuarioTitulo));
    }
}
