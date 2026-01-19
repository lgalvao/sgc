package sgc.organizacao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.erros.ErroAccessoNegado;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.comum.erros.ErroValidacao;
import sgc.comum.repo.RepositorioComum;
import sgc.organizacao.dto.AdministradorDto;
import sgc.organizacao.dto.PerfilDto;
import sgc.organizacao.dto.UsuarioDto;
import sgc.organizacao.model.*;

import java.util.*;

import static java.util.stream.Collectors.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class UsuarioFacade {
    private static final String ENTIDADE_USUARIO = "Usuário";
    private final UsuarioRepo usuarioRepo;
    private final UsuarioPerfilRepo usuarioPerfilRepo;
    private final AdministradorRepo administradorRepo;
    private final RepositorioComum repo;

    @Transactional(readOnly = true)
    public @Nullable Usuario carregarUsuarioParaAutenticacao(String titulo) {
        Usuario usuario = usuarioRepo.findByIdWithAtribuicoes(titulo).orElse(null);
        if (usuario != null) {
            carregarAtribuicoesUsuario(usuario);
            usuario.getAuthorities();
        }
        return usuario;
    }

    public Optional<UsuarioDto> buscarUsuarioPorTitulo(String titulo) {
        return usuarioRepo.findById(titulo).map(this::toUsuarioDto);
    }

    public List<UsuarioDto> buscarUsuariosPorUnidade(Long codigoUnidade) {
        return usuarioRepo.findByUnidadeLotacaoCodigo(codigoUnidade).stream()
                .map(this::toUsuarioDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public Usuario buscarPorId(String titulo) {
        return repo.buscar(Usuario.class, titulo);
    }

    @Transactional(readOnly = true)
    public Usuario buscarPorLogin(String login) {
        return buscarPorLoginInterno(login);
    }

    private Usuario buscarPorLoginInterno(String login) {
        Usuario usuario = usuarioRepo
                .findByIdWithAtribuicoes(login)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada(ENTIDADE_USUARIO, login));

        carregarAtribuicoesUsuario(usuario);
        return usuario;
    }

    private Optional<String> obterTituloUsuarioAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }
        return Optional.of(authentication.getName());
    }

    @Transactional(readOnly = true)
    public Usuario obterUsuarioAutenticado() {
        return obterTituloUsuarioAutenticado()
                .map(this::buscarPorLoginInterno)
                .orElseThrow(() -> new ErroAccessoNegado("Nenhum usuário autenticado no contexto"));
    }

    @Transactional(readOnly = true)
    public @Nullable Usuario obterUsuarioAutenticadoOuNull() {
        return obterTituloUsuarioAutenticado()
                .map(this::buscarPorLoginInterno)
                .orElse(null);
    }


    @Transactional(readOnly = true)
    public List<PerfilDto> buscarPerfisUsuario(String titulo) {
        return usuarioRepo.findByIdWithAtribuicoes(titulo)
                .map(usuario -> {
                    carregarAtribuicoesUsuario(usuario);
                    return usuario.getTodasAtribuicoes().stream()
                            .map(this::toPerfilDto)
                            .toList();
                })
                .orElse(Collections.emptyList());
    }

    private void carregarAtribuicoesUsuario(Usuario usuario) {
        List<UsuarioPerfil> atribuicoes = usuarioPerfilRepo.findByUsuarioTitulo(usuario.getTituloEleitoral());
        usuario.setAtribuicoes(new HashSet<>(atribuicoes));
    }

    private void carregarAtribuicoesEmLote(List<Usuario> usuarios) {
        if (usuarios == null || usuarios.isEmpty()) {
            return;
        }

        List<String> titulos = usuarios.stream()
                .map(Usuario::getTituloEleitoral)
                .toList();

        List<UsuarioPerfil> todasAtribuicoes = usuarioPerfilRepo.findByUsuarioTituloIn(titulos);

        Map<String, List<UsuarioPerfil>> porUsuario = todasAtribuicoes.stream()
                .collect(groupingBy(UsuarioPerfil::getUsuarioTitulo));

        usuarios.forEach(u -> {
            List<UsuarioPerfil> atribuicoes = porUsuario.getOrDefault(u.getTituloEleitoral(), Collections.emptyList());
            u.setAtribuicoes(new HashSet<>(atribuicoes));
        });
    }

    public Optional<UsuarioDto> buscarUsuarioPorEmail(String email) {
        return usuarioRepo.findByEmail(email).map(this::toUsuarioDto);
    }

    public List<UsuarioDto> buscarUsuariosAtivos() {
        return usuarioRepo.findAll().stream().map(this::toUsuarioDto).toList();
    }

    public Map<String, UsuarioDto> buscarUsuariosPorTitulos(List<String> titulos) {
        return usuarioRepo.findAllById(titulos).stream()
                .collect(toMap(Usuario::getTituloEleitoral, this::toUsuarioDto));
    }

    @Transactional(readOnly = true)
    public List<Long> buscarUnidadesOndeEhResponsavel(String tituloUsuario) {
        return usuarioRepo
                .findByIdWithAtribuicoes(tituloUsuario)
                .map(u -> {
                    carregarAtribuicoesUsuario(u);
                    return u.getTodasAtribuicoes().stream()
                            .filter(a -> a.getPerfil() == Perfil.CHEFE)
                            .map(UsuarioPerfil::getUnidadeCodigo)
                            .toList();
                })
                .orElse(Collections.emptyList());
    }

    @Transactional(readOnly = true)
    public boolean usuarioTemPerfil(String titulo, Perfil perfil, Long unidadeCodigo) {
        return usuarioRepo
                .findByIdWithAtribuicoes(titulo)
                .map(u -> {
                    carregarAtribuicoesUsuario(u);
                    return u.getTodasAtribuicoes().stream()
                            .anyMatch(a -> a.getPerfil() == perfil
                                    && a.getUnidadeCodigo().equals(unidadeCodigo));
                })
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public List<Long> buscarUnidadesPorPerfil(String titulo, Perfil perfil) {
        return usuarioRepo
                .findByIdWithAtribuicoes(titulo)
                .map(u -> {
                    carregarAtribuicoesUsuario(u);
                    return u.getTodasAtribuicoes().stream()
                            .filter(a -> a.getPerfil() == perfil)
                            .map(a -> a.getUnidade().getCodigo())
                            .toList();
                })
                .orElse(Collections.emptyList());
    }

    private UsuarioDto toUsuarioDto(Usuario usuario) {
        Unidade lotacao = usuario.getUnidadeLotacao();
        return UsuarioDto.builder()
                .tituloEleitoral(usuario.getTituloEleitoral())
                .nome(usuario.getNome())
                .email(usuario.getEmail())
                .matricula(usuario.getMatricula())
                .unidadeCodigo(lotacao.getCodigo())
                .build();
    }

    private PerfilDto toPerfilDto(UsuarioPerfil atribuicao) {
        return PerfilDto.builder()
                .usuarioTitulo(atribuicao.getUsuario().getTituloEleitoral())
                .unidadeCodigo(atribuicao.getUnidade().getCodigo())
                .unidadeNome(atribuicao.getUnidade().getNome())
                .perfil(atribuicao.getPerfil().name())
                .build();
    }

    @Transactional(readOnly = true)
    public List<AdministradorDto> listarAdministradores() {
        return administradorRepo.findAll().stream()
                .flatMap(admin -> usuarioRepo.findById(admin.getUsuarioTitulo())
                        .map(this::toAdministradorDto)
                        .stream())
                .toList();
    }

    @Transactional
    public AdministradorDto adicionarAdministrador(String usuarioTitulo) {
        Usuario usuario = repo.buscar(Usuario.class, usuarioTitulo);

        if (administradorRepo.existsById(usuarioTitulo)) {
            throw new ErroValidacao("Usuário já é administrador");
        }

        Administrador administrador = new Administrador(usuarioTitulo);
        administradorRepo.save(administrador);

        log.info("Administrador {} adicionado", usuarioTitulo);
        return toAdministradorDto(usuario);
    }

    @Transactional
    public void removerAdministrador(String usuarioTitulo, String usuarioAtualTitulo) {
        if (usuarioTitulo.equals(usuarioAtualTitulo)) {
            throw new ErroValidacao("Não é permitido remover a si mesmo como administrador");
        }

        if (!administradorRepo.existsById(usuarioTitulo)) {
            throw new ErroValidacao("Usuário informado não é um administrador");
        }

        long totalAdministradores = administradorRepo.count();
        if (totalAdministradores <= 1) {
            throw new ErroValidacao("Não é permitido remover o único administrador do sistema");
        }

        administradorRepo.deleteById(usuarioTitulo);
        log.info("Administrador {} removido com sucesso", usuarioTitulo);
    }

    @Transactional(readOnly = true)
    public boolean isAdministrador(String usuarioTitulo) {
        return administradorRepo.existsById(usuarioTitulo);
    }

    private AdministradorDto toAdministradorDto(Usuario usuario) {
        Unidade unidadeLotacao = usuario.getUnidadeLotacao();

        return AdministradorDto.builder()
                .tituloEleitoral(usuario.getTituloEleitoral())
                .nome(usuario.getNome())
                .matricula(usuario.getMatricula())
                .unidadeCodigo(unidadeLotacao.getCodigo())
                .unidadeSigla(unidadeLotacao.getSigla())
                .build();
    }

    public @Nullable String extrairTituloUsuario(@Nullable Object principal) {
        if (principal instanceof String string) return string;
        if (principal instanceof Usuario usuario) return usuario.getTituloEleitoral();

        return principal != null ? principal.toString() : null;
    }
}
