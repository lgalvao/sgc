package sgc.organizacao.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.erros.ErroValidacao;
import sgc.comum.repo.ComumRepo;
import sgc.organizacao.model.Administrador;
import sgc.organizacao.model.AdministradorRepo;
import sgc.organizacao.model.Usuario;
import sgc.organizacao.model.UsuarioPerfil;
import sgc.organizacao.model.UsuarioPerfilRepo;
import sgc.organizacao.model.UsuarioRepo;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Serviço consolidado para operações de Usuário.
 *
 * <p>Responsabilidades:
 * <ul>
 *   <li>Consultas básicas de usuários (por ID, email, unidade)</li>
 *   <li>Gerenciamento de perfis de usuários</li>
 *   <li>Gerenciamento de administradores do sistema</li>
 * </ul>
 *
 * <p>Este serviço consolida:
 * <ul>
 *   <li>UsuarioConsultaService (wrapper eliminado)</li>
 *   <li>UsuarioPerfilService (perfis)</li>
 *   <li>AdministradorService (administradores)</li>
 * </ul>
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UsuarioService {
    private final UsuarioRepo usuarioRepo;
    private final UsuarioPerfilRepo usuarioPerfilRepo;
    private final AdministradorRepo administradorRepo;
    private final ComumRepo repo;

    // ========== Consultas Básicas ==========

    public Usuario buscarPorId(String titulo) {
        return repo.buscar(Usuario.class, titulo);
    }

    public Optional<Usuario> buscarPorIdOpcional(String titulo) {
        return usuarioRepo.findById(titulo);
    }

    public Usuario buscarPorIdComAtribuicoes(String titulo) {
        return repo.buscar(Usuario.class, titulo);
    }

    public Optional<Usuario> buscarPorIdComAtribuicoesOpcional(String titulo) {
        return usuarioRepo.findByIdWithAtribuicoes(titulo);
    }

    public List<Usuario> buscarPorUnidadeLotacao(Long codigoUnidade) {
        return usuarioRepo.findByUnidadeLotacaoCodigo(codigoUnidade);
    }

    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarioRepo.findByEmail(email);
    }

    public List<Usuario> buscarTodos() {
        return usuarioRepo.findAll();
    }

    public List<Usuario> buscarTodosPorIds(List<String> titulos) {
        return usuarioRepo.findAllById(titulos);
    }

    // ========== Perfis ==========

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

    // ========== Administradores ==========

    public List<Administrador> listarAdministradores() {
        return administradorRepo.findAll();
    }

    @Transactional
    public void adicionarAdministrador(String usuarioTitulo) {
        if (administradorRepo.existsById(usuarioTitulo)) {
            throw new ErroValidacao("Usuário já é administrador");
        }

        Administrador administrador = Administrador.builder()
                .usuarioTitulo(usuarioTitulo)
                .build();
        administradorRepo.save(administrador);
    }

    @Transactional
    public void removerAdministrador(String usuarioTitulo) {
        if (!administradorRepo.existsById(usuarioTitulo)) {
            throw new ErroValidacao("Usuário informado não é um administrador");
        }

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
