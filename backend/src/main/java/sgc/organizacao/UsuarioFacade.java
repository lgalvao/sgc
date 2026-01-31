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
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.comum.erros.ErroValidacao;

import sgc.organizacao.dto.AdministradorDto;
import sgc.organizacao.dto.PerfilDto;
import sgc.organizacao.dto.UnidadeResponsavelDto;
import sgc.organizacao.dto.UsuarioDto;
import sgc.organizacao.model.*;
import sgc.organizacao.service.AdministradorService;
import sgc.organizacao.service.UnidadeConsultaService;
import sgc.organizacao.service.UsuarioConsultaService;
import sgc.organizacao.service.UsuarioPerfilService;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class UsuarioFacade {
    private final UsuarioConsultaService usuarioConsultaService;
    private final UsuarioPerfilService usuarioPerfilService;
    private final AdministradorService administradorService;
    private final UnidadeConsultaService unidadeConsultaService;

    @Transactional(readOnly = true)
    public @Nullable Usuario carregarUsuarioParaAutenticacao(String titulo) {
        Usuario usuario = usuarioConsultaService.buscarPorIdComAtribuicoesOpcional(titulo).orElse(null);
        if (usuario != null) {
            carregarAtribuicoes(usuario);
        }
        return usuario;
    }

    public Optional<UsuarioDto> buscarUsuarioPorTitulo(String titulo) {
        return usuarioConsultaService.buscarPorIdOpcional(titulo).map(this::toUsuarioDto);
    }

    public List<UsuarioDto> buscarUsuariosPorUnidade(Long codigoUnidade) {
        return usuarioConsultaService.buscarPorUnidadeLotacao(codigoUnidade).stream()
                .map(this::toUsuarioDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public Usuario buscarPorId(String titulo) {
        return usuarioConsultaService.buscarPorId(titulo);
    }

    @Transactional(readOnly = true)
    public Usuario buscarPorLogin(String login) {
        return buscarPorLoginInterno(login);
    }

    private Usuario buscarPorLoginInterno(String login) {
        Usuario usuario = usuarioConsultaService.buscarPorIdComAtribuicoes(login);
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
        Unidade unidade = unidadeConsultaService.buscarPorSigla(sigla);

        Usuario usuarioSimples = usuarioConsultaService.buscarChefePorUnidade(unidade.getCodigo(), sigla);

        Usuario usuarioCompleto = usuarioConsultaService.buscarPorIdComAtribuicoes(usuarioSimples.getTituloEleitoral());

        carregarAtribuicoes(usuarioCompleto);
        return usuarioCompleto;
    }

    @Transactional(readOnly = true)
    public List<PerfilDto> buscarPerfisUsuario(String titulo) {
        return usuarioConsultaService.buscarPorIdComAtribuicoesOpcional(titulo)
                .map(usuario -> {
                    Set<UsuarioPerfil> atribuicoes = new HashSet<>(
                        usuarioPerfilService.buscarPorUsuario(usuario.getTituloEleitoral())
                    );
                    return usuario.getTodasAtribuicoes(atribuicoes).stream()
                            .filter(a -> a.getUnidade().getSituacao() == SituacaoUnidade.ATIVA)
                            .map(this::toPerfilDto)
                            .toList();
                })
                .orElse(Collections.emptyList());
    }

    private void carregarAtribuicoes(Usuario usuario) {
        usuarioPerfilService.carregarAuthorities(usuario);
    }

    public Optional<UsuarioDto> buscarUsuarioPorEmail(String email) {
        return usuarioConsultaService.buscarPorEmail(email).map(this::toUsuarioDto);
    }

    public List<UsuarioDto> buscarUsuariosAtivos() {
        return usuarioConsultaService.buscarTodos().stream().map(this::toUsuarioDto).toList();
    }

    public UnidadeResponsavelDto buscarResponsavelUnidade(Long unidadeCodigo) {
        List<Usuario> chefes = usuarioConsultaService.buscarChefesPorUnidades(List.of(unidadeCodigo));
        if (chefes.isEmpty()) {
            throw new ErroEntidadeNaoEncontrada("Responsável da unidade", unidadeCodigo);
        }
        return montarResponsavelDto(unidadeCodigo, chefes);
    }

    @Transactional(readOnly = true)
    public Map<Long, UnidadeResponsavelDto> buscarResponsaveisUnidades(List<Long> unidadesCodigos) {
        if (unidadesCodigos.isEmpty()) return Collections.emptyMap();

        List<Usuario> todosChefes = usuarioConsultaService.buscarChefesPorUnidades(unidadesCodigos);
        if (todosChefes.isEmpty()) return Collections.emptyMap();

        List<String> titulos = todosChefes.stream().map(Usuario::getTituloEleitoral).toList();
        List<Usuario> chefesCompletos = usuarioConsultaService.buscarPorIdsComAtribuicoes(titulos);
        
        // Carregar atribuições para cada usuário
        Map<String, Set<UsuarioPerfil>> atribuicoesPorUsuario = new HashMap<>();
        for (Usuario usuario : chefesCompletos) {
            Set<UsuarioPerfil> atribuicoes = usuarioPerfilService.buscarAtribuicoesParaCache(usuario.getTituloEleitoral());
            atribuicoesPorUsuario.put(usuario.getTituloEleitoral(), atribuicoes);
        }

        Map<Long, List<Usuario>> chefesPorUnidade = chefesCompletos.stream()
                .flatMap(u -> u.getTodasAtribuicoes(atribuicoesPorUsuario.get(u.getTituloEleitoral())).stream()
                        .filter(a -> a.getUnidade().getSituacao() == SituacaoUnidade.ATIVA)
                        .filter(a -> a.getPerfil() == Perfil.CHEFE && unidadesCodigos.contains(a.getUnidadeCodigo()))
                        .map(a -> new AbstractMap.SimpleEntry<>(a.getUnidadeCodigo(), u)))
                .collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.mapping(Map.Entry::getValue, Collectors.toList())));

        return chefesPorUnidade.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> montarResponsavelDto(e.getKey(), e.getValue())
                ));
    }

    public Map<String, UsuarioDto> buscarUsuariosPorTitulos(List<String> titulos) {
        return usuarioConsultaService.buscarTodosPorIds(titulos).stream()
                .collect(toMap(Usuario::getTituloEleitoral, this::toUsuarioDto, (u1, u2) -> u1));
    }

    @Transactional(readOnly = true)
    public List<Long> buscarUnidadesOndeEhResponsavel(String titulo) {
        return usuarioConsultaService.buscarPorIdComAtribuicoesOpcional(titulo)
                .map(u -> {
                    Set<UsuarioPerfil> atribuicoes = new HashSet<>(
                        usuarioPerfilService.buscarPorUsuario(u.getTituloEleitoral())
                    );
                    return u.getTodasAtribuicoes(atribuicoes).stream()
                            .filter(a -> a.getUnidade().getSituacao() == SituacaoUnidade.ATIVA)
                            .filter(a -> a.getPerfil() == Perfil.CHEFE)
                            .map(UsuarioPerfil::getUnidadeCodigo)
                            .toList();
                })
                .orElse(Collections.emptyList());
    }

    @Transactional(readOnly = true)
    public boolean usuarioTemPerfil(String titulo, String perfil, Long unidadeCodigo) {
        return usuarioConsultaService.buscarPorIdComAtribuicoesOpcional(titulo)
                .map(u -> {
                    Set<UsuarioPerfil> atribuicoes = new HashSet<>(
                        usuarioPerfilService.buscarPorUsuario(u.getTituloEleitoral())
                    );
                    return u.getTodasAtribuicoes(atribuicoes).stream()
                            .filter(a -> a.getUnidade().getSituacao() == SituacaoUnidade.ATIVA)
                            .anyMatch(a -> a.getPerfil().name().equals(perfil)
                                    && a.getUnidadeCodigo().equals(unidadeCodigo));
                })
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public List<Long> buscarUnidadesPorPerfil(String titulo, String perfil) {
        return usuarioConsultaService.buscarPorIdComAtribuicoesOpcional(titulo)
                .map(u -> {
                    Set<UsuarioPerfil> atribuicoes = new HashSet<>(
                        usuarioPerfilService.buscarPorUsuario(u.getTituloEleitoral())
                    );
                    return u.getTodasAtribuicoes(atribuicoes).stream()
                            .filter(a -> a.getUnidade().getSituacao() == SituacaoUnidade.ATIVA)
                            .filter(a -> a.getPerfil().name().equals(perfil))
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

    private UnidadeResponsavelDto montarResponsavelDto(Long unidadeCodigo, List<Usuario> chefes) {
        Usuario titular = chefes.getFirst();
        Usuario substituto = chefes.size() > 1 ? chefes.get(1) : null;

        return UnidadeResponsavelDto.builder()
                .unidadeCodigo(unidadeCodigo)
                .titularTitulo(titular.getTituloEleitoral())
                .titularNome(titular.getNome())
                .substitutoTitulo(substituto != null ? substituto.getTituloEleitoral() : null)
                .substitutoNome(substituto != null ? substituto.getNome() : null)
                .build();
    }

    @Transactional(readOnly = true)
    public List<AdministradorDto> listarAdministradores() {
        return administradorService.listarTodos().stream()
                .flatMap(admin -> usuarioConsultaService.buscarPorIdOpcional(admin.getUsuarioTitulo())
                        .map(this::toAdministradorDto)
                        .stream())
                .toList();
    }

    @Transactional
    public AdministradorDto adicionarAdministrador(String usuarioTitulo) {
        Usuario usuario = usuarioConsultaService.buscarPorId(usuarioTitulo);

        administradorService.adicionar(usuarioTitulo);

        log.info("Administrador {} adicionado", usuarioTitulo);
        return toAdministradorDto(usuario);
    }

    @Transactional
    public void removerAdministrador(String usuarioTitulo, String usuarioAtualTitulo) {
        if (usuarioTitulo.equals(usuarioAtualTitulo)) {
            throw new ErroValidacao("Não é permitido remover a si mesmo como administrador");
        }

        administradorService.remover(usuarioTitulo);
        log.info("Administrador {} removido com sucesso", usuarioTitulo);
    }

    @Transactional(readOnly = true)
    public boolean isAdministrador(String usuarioTitulo) {
        return administradorService.isAdministrador(usuarioTitulo);
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
