package sgc.organizacao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.erros.ErroAcessoNegado;
import sgc.comum.erros.ErroValidacao;
import sgc.organizacao.dto.AdministradorDto;
import sgc.organizacao.dto.PerfilDto;
import sgc.organizacao.dto.UnidadeResponsavelDto;
import sgc.organizacao.model.SituacaoUnidade;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.organizacao.model.UsuarioPerfil;
import sgc.organizacao.service.ResponsavelUnidadeService;
import sgc.organizacao.service.UsuarioService;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class UsuarioFacade {
    private final UsuarioService usuarioService;
    private final ResponsavelUnidadeService responsavelUnidadeService;

    @Transactional(readOnly = true)
    public @Nullable Usuario carregarUsuarioParaAutenticacao(String titulo) {
        Usuario usuario = usuarioService.buscarComAtribuicoesOpt(titulo).orElse(null);
        if (usuario != null) {
            carregarAtribuicoes(usuario);
        }
        return usuario;
    }

    public Optional<Usuario> buscarUsuarioPorTitulo(String titulo) {
        return usuarioService.buscarOpt(titulo);
    }

    public Optional<Usuario> buscarEntidadeUsuarioPorTitulo(String titulo) {
        return usuarioService.buscarOpt(titulo);
    }

    public List<Usuario> buscarUsuariosPorUnidade(Long codigoUnidade) {
        return usuarioService.buscarPorUnidadeLotacao(codigoUnidade);
    }

    @Transactional(readOnly = true)
    public Usuario buscarPorId(String titulo) {
        return usuarioService.buscar(titulo);
    }

    @Transactional(readOnly = true)
    public Usuario buscarPorLogin(String login) {
        return buscarPorLoginInterno(login);
    }

    private Usuario buscarPorLoginInterno(String login) {
        Usuario usuario = usuarioService.buscarComAtribuicoes(login);
        carregarAtribuicoes(usuario);
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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Usuario usuario) {
            return usuario;
        }
        return obterTituloUsuarioAutenticado()
                .map(this::buscarPorLoginInterno)
                .orElseThrow(() -> new ErroAcessoNegado("Nenhum usuário autenticado no contexto"));
    }

    @Transactional(readOnly = true)
    public @Nullable Usuario obterUsuarioAutenticadoOuNull() {
        return obterTituloUsuarioAutenticado()
                .map(this::buscarPorLoginInterno)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public Usuario buscarResponsavelAtual(String sigla) {
        return responsavelUnidadeService.buscarResponsavelAtual(sigla);
    }

    @Transactional(readOnly = true)
    public List<PerfilDto> buscarPerfisUsuario(String titulo) {
        return usuarioService.buscarComAtribuicoesOpt(titulo)
                .map(usuario -> {
                    List<UsuarioPerfil> atribuicoes = usuarioService.buscarPerfis(usuario.getTituloEleitoral());
                    return atribuicoes.stream()
                            .filter(a -> a.getUnidade().getSituacao() == SituacaoUnidade.ATIVA)
                            .map(this::toPerfilDto)
                            .toList();
                })
                .orElse(Collections.emptyList());
    }

    private void carregarAtribuicoes(Usuario usuario) {
        usuarioService.carregarAuthorities(usuario);
    }

    public List<Usuario> buscarUsuariosAtivos() {
        return usuarioService.buscarTodos();
    }

    @SuppressWarnings("unused")
    public UnidadeResponsavelDto buscarResponsavelUnidade(Long unidadeCodigo) {
        return responsavelUnidadeService.buscarResponsavelUnidade(unidadeCodigo);
    }

    @Transactional(readOnly = true)
    public Map<Long, UnidadeResponsavelDto> buscarResponsaveisUnidades(List<Long> unidadesCodigos) {
        if (unidadesCodigos.isEmpty()) {
            return Collections.emptyMap();
        }
        return responsavelUnidadeService.buscarResponsaveisUnidades(unidadesCodigos);
    }

    public Map<String, Usuario> buscarUsuariosPorTitulos(List<String> titulos) {
        return usuarioService.buscarPorTitulos(titulos).stream()
                .collect(toMap(Usuario::getTituloEleitoral, u -> u, (u1, u2) -> u1));
    }

    @Transactional(readOnly = true)
    public List<Long> buscarUnidadesOndeEhResponsavel(String titulo) {
        return responsavelUnidadeService.buscarUnidadesOndeEhResponsavel(titulo);
    }

    @Transactional(readOnly = true)
    public boolean usuarioTemPerfil(String titulo, String perfil, Long unidadeCodigo) {
        return usuarioService.buscarComAtribuicoesOpt(titulo)
                .map(u -> {
                    List<UsuarioPerfil> atribuicoes = usuarioService.buscarPerfis(u.getTituloEleitoral());
                    return atribuicoes.stream()
                            .filter(a -> a.getUnidade().getSituacao() == SituacaoUnidade.ATIVA)
                            .anyMatch(a -> a.getPerfil().name().equals(perfil)
                                    && a.getUnidadeCodigo().equals(unidadeCodigo));
                })
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public List<Long> buscarUnidadesPorPerfil(String titulo, String perfil) {
        return usuarioService.buscarComAtribuicoesOpt(titulo)
                .map(u -> {
                    List<UsuarioPerfil> atribuicoes = usuarioService.buscarPerfis(u.getTituloEleitoral());
                    return atribuicoes.stream()
                            .filter(a -> a.getUnidade().getSituacao() == SituacaoUnidade.ATIVA)
                            .filter(a -> a.getPerfil().name().equals(perfil))
                            .map(a -> a.getUnidade().getCodigo())
                            .toList();
                })
                .orElse(Collections.emptyList());
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
        return usuarioService.buscarAdministradores().stream()
                .flatMap(admin -> usuarioService.buscarOpt(admin.getUsuarioTitulo())
                        .map(this::toAdministradorDto)
                        .stream())
                .toList();
    }

    @Transactional
    public AdministradorDto adicionarAdministrador(String usuarioTitulo) {
        Usuario usuario = usuarioService.buscar(usuarioTitulo);

        usuarioService.adicionarAdministrador(usuarioTitulo);

        log.info("Administrador {} adicionado", usuarioTitulo);
        return toAdministradorDto(usuario);
    }

    @Transactional
    public void removerAdministrador(String usuarioTitulo, String usuarioAtualTitulo) {
        if (usuarioTitulo.equals(usuarioAtualTitulo)) {
            throw new ErroValidacao("Não é permitido remover a si mesmo como administrador");
        }

        usuarioService.removerAdministrador(usuarioTitulo);
        log.info("Administrador {} removido com sucesso", usuarioTitulo);
    }

    @Transactional(readOnly = true)
    public boolean isAdministrador(String usuarioTitulo) {
        return usuarioService.isAdministrador(usuarioTitulo);
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
