package sgc.organizacao.service;

import lombok.*;
import lombok.extern.slf4j.*;
import org.springframework.security.core.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import sgc.comum.erros.*;
import sgc.comum.model.*;
import sgc.organizacao.model.*;

import java.util.*;
import java.util.stream.*;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UsuarioService {
    private final UsuarioRepo usuarioRepo;
    private final UsuarioPerfilRepo usuarioPerfilRepo;
    private final AdministradorRepo administradorRepo;
    private final ComumRepo repo;

    public Usuario buscar(String titulo) {
        return repo.buscar(Usuario.class, titulo);
    }

    public Optional<Usuario> buscarOpt(String titulo) {
        return usuarioRepo.findById(titulo);
    }

    public Usuario buscarComAtribuicoes(String titulo) {
        return repo.buscar(Usuario.class, titulo);
    }

    public Optional<Usuario> buscarComAtribuicoesOpt(String titulo) {
        return usuarioRepo.findByIdWithAtribuicoes(titulo);
    }

    public List<Usuario> buscarPorUnidadeLotacao(Long codUnidade) {
        return usuarioRepo.findByUnidadeLotacaoCodigo(codUnidade);
    }

    public List<Usuario> buscarTodos() {
        return usuarioRepo.findAll();
    }

    public List<Usuario> buscarPorTitulos(List<String> titulos) {
        return usuarioRepo.findAllById(titulos);
    }

    public List<UsuarioPerfil> buscarPerfis(String usuarioTitulo) {
        return usuarioPerfilRepo.findByUsuarioTitulo(usuarioTitulo);
    }

    public void carregarAuthorities(Usuario usuario) {
        List<UsuarioPerfil> perfis = usuarioPerfilRepo.findByUsuarioTitulo(usuario.getTituloEleitoral());

        Set<GrantedAuthority> authorities = perfis.stream()
                .map(a -> a.getPerfil().toGrantedAuthority())
                .collect(Collectors.toSet());

        usuario.setAuthorities(authorities);
    }

    public List<Administrador> buscarAdministradores() {
        return administradorRepo.findAll();
    }

    @Transactional
    public void adicionarAdministrador(String usuarioTitulo) {
        if (isAdministrador(usuarioTitulo)) {
            throw new ErroValidacao("Usuário já é um administrador do sistema");
        }
        Administrador administrador = Administrador.builder()
                .usuarioTitulo(usuarioTitulo)
                .build();

        administradorRepo.save(administrador);
    }

    @Transactional
    public void removerAdministrador(String usuarioTitulo) {
        long totalAdministradores = administradorRepo.count();
        if (totalAdministradores <= 1) {
            throw new ErroValidacao("Não é permitido remover o único administrador do sistema");
        }
        administradorRepo.deleteById(usuarioTitulo);
    }

    public boolean isAdministrador(String usuarioTitulo) {
        return administradorRepo.existsById(usuarioTitulo);
    }
}
