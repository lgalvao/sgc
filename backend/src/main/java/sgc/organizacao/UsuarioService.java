package sgc.organizacao;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.erros.ErroAccessoNegado;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.comum.erros.ErroValidacao;
import sgc.organizacao.dto.*;
import sgc.organizacao.model.*;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

@Service
@Slf4j
public class UsuarioService {
    private static final String ENTIDADE_USUARIO = "Usuário";
    private final UsuarioRepo usuarioRepo;
    private final UsuarioPerfilRepo usuarioPerfilRepo;
    private final AdministradorRepo administradorRepo;
    @Lazy
    private final UnidadeService unidadeService;

    @Transactional(readOnly = true)
    public @Nullable Usuario carregarUsuarioParaAutenticacao(String titulo) {
        Usuario usuario = usuarioRepo.findByIdWithAtribuicoes(titulo).orElse(null);
        if (usuario != null) {
            carregarAtribuicoes(usuario);
            usuario.getAuthorities();
        }
        return usuario;
    }

    public UsuarioService(UsuarioRepo usuarioRepo,
            UsuarioPerfilRepo usuarioPerfilRepo,
            AdministradorRepo administradorRepo,
            @Lazy UnidadeService unidadeService) {
        this.usuarioRepo = usuarioRepo;
        this.usuarioPerfilRepo = usuarioPerfilRepo;
        this.administradorRepo = administradorRepo;
        this.unidadeService = unidadeService;
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
        return usuarioRepo
                .findById(titulo)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada(ENTIDADE_USUARIO, titulo));
    }

    @Transactional(readOnly = true)
    public Usuario buscarPorLogin(String login) {
        return buscarPorLoginInterno(login);
    }

    private Usuario buscarPorLoginInterno(String login) {
        Usuario usuario = usuarioRepo
                .findByIdWithAtribuicoes(login)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada(ENTIDADE_USUARIO, login));

        carregarAtribuicoes(usuario);
        return usuario;
    }

    /**
     * Obtém o usuário atualmente autenticado a partir do contexto de segurança do
     * Spring.
     * 
     * @return O usuário autenticado
     * @throws ErroAccessoNegado se não houver usuário autenticado
     */
    @Transactional(readOnly = true)
    public Usuario obterUsuarioAutenticado() {
        org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof org.springframework.security.authentication.AnonymousAuthenticationToken) {
            throw new ErroAccessoNegado("Nenhum usuário autenticado no contexto");
        }

        String tituloEleitoral = authentication.getName();
        return buscarPorLoginInterno(tituloEleitoral);
    }

    @Transactional(readOnly = true)
    public Usuario buscarResponsavelAtual(String sigla) {
        UnidadeDto unidadeDto = unidadeService.buscarPorSigla(sigla);

        // Primeiro busca o chefe (pode ser lazy)
        Usuario usuarioSimples = usuarioRepo
                .chefePorCodUnidade(unidadeDto.getCodigo())
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Responsável da unidade", sigla));

        // Recarrega com join fetch para garantir as atribuições
        Usuario usuarioCompleto = usuarioRepo.findByIdWithAtribuicoes(usuarioSimples.getTituloEleitoral())
                .orElseThrow(
                        () -> new ErroEntidadeNaoEncontrada(ENTIDADE_USUARIO, usuarioSimples.getTituloEleitoral()));

        carregarAtribuicoes(usuarioCompleto);
        return usuarioCompleto;
    }

    @Transactional(readOnly = true)
    public List<PerfilDto> buscarPerfisUsuario(String titulo) {
        return usuarioRepo.findByIdWithAtribuicoes(titulo)
                .map(usuario -> {
                    carregarAtribuicoes(usuario);
                    return usuario.getTodasAtribuicoes().stream()
                            .map(this::toPerfilDto)
                            .toList();
                })
                .orElse(Collections.emptyList());
    }

    private void carregarAtribuicoes(Usuario usuario) {
        var atribuicoes = usuarioPerfilRepo.findByUsuarioTitulo(usuario.getTituloEleitoral());
        usuario.setAtribuicoes(new java.util.HashSet<>(atribuicoes));
    }

    private void carregarAtribuicoesEmLote(List<Usuario> usuarios) {
        if (usuarios.isEmpty())
            return;

        List<String> titulos = usuarios.stream()
                .map(Usuario::getTituloEleitoral)
                .toList();

        List<UsuarioPerfil> todasAtribuicoes = usuarioPerfilRepo.findByUsuarioTituloIn(titulos);

        Map<String, Set<UsuarioPerfil>> atribuicoesPorUsuario = todasAtribuicoes.stream()
                .collect(Collectors.groupingBy(
                        UsuarioPerfil::getUsuarioTitulo,
                        Collectors.toSet()));

        for (Usuario usuario : usuarios) {
            Set<UsuarioPerfil> atribuicoes = atribuicoesPorUsuario
                    .getOrDefault(usuario.getTituloEleitoral(), new java.util.HashSet<>());
            usuario.setAtribuicoes(atribuicoes);
        }
    }

    public Optional<UsuarioDto> buscarUsuarioPorEmail(String email) {
        return usuarioRepo.findByEmail(email).map(this::toUsuarioDto);
    }

    public List<UsuarioDto> buscarUsuariosAtivos() {
        return usuarioRepo.findAll().stream().map(this::toUsuarioDto).toList();
    }

    public Optional<UnidadeDto> buscarUnidadePorCodigo(Long codigo) {
        try {
            return Optional.of(unidadeService.buscarPorCodigo(codigo));
        } catch (ErroEntidadeNaoEncontrada e) {
            return Optional.empty();
        }
    }

    public Optional<UnidadeDto> buscarUnidadePorSigla(String sigla) {
        try {
            return Optional.of(unidadeService.buscarPorSigla(sigla));
        } catch (ErroEntidadeNaoEncontrada e) {
            return Optional.empty();
        }
    }

    public List<UnidadeDto> buscarUnidadesAtivas() {
        return unidadeService.buscarTodasUnidades();
    }

    public List<UnidadeDto> buscarSubunidades(Long codigoPai) {
        return unidadeService.listarSubordinadas(codigoPai).stream()
                .map(this::toUnidadeDto)
                .toList();
    }

    public List<UnidadeDto> construirArvoreHierarquica() {
        return unidadeService.buscarArvoreHierarquica();
    }

    public Optional<ResponsavelDto> buscarResponsavelUnidade(Long unidadeCodigo) {
        List<Usuario> chefes = usuarioRepo.findChefesByUnidadesCodigos(List.of(unidadeCodigo));
        return chefes.isEmpty()
                ? Optional.empty()
                : Optional.of(montarResponsavelDto(unidadeCodigo, chefes));
    }

    @Transactional(readOnly = true)
    public Map<Long, ResponsavelDto> buscarResponsaveisUnidades(List<Long> unidadesCodigos) {
        List<Usuario> todosChefes = usuarioRepo.findChefesByUnidadesCodigos(unidadesCodigos);

        if (todosChefes.isEmpty()) {
            return Collections.emptyMap();
        }

        // Carrega todos os usuários com atribuições em uma única query (elimina N+1)
        List<String> titulos = todosChefes.stream()
                .map(Usuario::getTituloEleitoral)
                .toList();

        List<Usuario> chefesCompletos = usuarioRepo.findByIdInWithAtribuicoes(titulos);

        // Carrega perfis em lote
        carregarAtribuicoesEmLote(chefesCompletos);

        Map<Long, List<Usuario>> chefesPorUnidade = chefesCompletos.stream()
                .flatMap(u -> u.getTodasAtribuicoes().stream()
                        .filter(a -> a.getPerfil() == Perfil.CHEFE
                                && unidadesCodigos.contains(
                                        a.getUnidadeCodigo()))
                        .map(a -> new AbstractMap.SimpleEntry<>(a.getUnidadeCodigo(), u)))
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toList())));

        Map<Long, ResponsavelDto> resultado = new HashMap<>();
        for (Long codigo : unidadesCodigos) {
            List<Usuario> chefes = chefesPorUnidade.getOrDefault(codigo, Collections.emptyList());
            if (!chefes.isEmpty()) {
                resultado.put(codigo, montarResponsavelDto(codigo, chefes));
            }
        }
        return resultado;
    }

    public Map<String, UsuarioDto> buscarUsuariosPorTitulos(List<String> titulos) {
        return usuarioRepo.findAllById(titulos).stream()
                .collect(toMap(Usuario::getTituloEleitoral, this::toUsuarioDto));
    }

    @Transactional(readOnly = true)
    public List<Long> buscarUnidadesOndeEhResponsavel(String titulo) {
        return usuarioRepo
                .findByIdWithAtribuicoes(titulo)
                .map(u -> {
                    carregarAtribuicoes(u);
                    return u.getTodasAtribuicoes().stream()
                            .filter(a -> a.getPerfil() == Perfil.CHEFE)
                            .map(UsuarioPerfil::getUnidadeCodigo)
                            .toList();
                })
                .orElse(Collections.emptyList());
    }

    @Transactional(readOnly = true)
    public boolean usuarioTemPerfil(String titulo, String perfil, Long unidadeCodigo) {
        return usuarioRepo
                .findByIdWithAtribuicoes(titulo)
                .map(u -> {
                    carregarAtribuicoes(u);
                    return u.getTodasAtribuicoes().stream()
                            .anyMatch(a -> a.getPerfil().name().equals(perfil)
                                    && a.getUnidadeCodigo().equals(unidadeCodigo));
                })
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public List<Long> buscarUnidadesPorPerfil(String titulo, String perfil) {
        return usuarioRepo
                .findByIdWithAtribuicoes(titulo)
                .map(u -> {
                    carregarAtribuicoes(u);
                    return u.getTodasAtribuicoes().stream()
                            .filter(a -> a.getPerfil().name().equals(perfil))
                            .map(a -> a.getUnidade().getCodigo())
                            .toList();
                })
                .orElse(Collections.emptyList());
    }

    private UsuarioDto toUsuarioDto(Usuario usuario) {
        return UsuarioDto.builder()
                .tituloEleitoral(usuario.getTituloEleitoral())
                .nome(usuario.getNome())
                .email(usuario.getEmail())
                .matricula(usuario.getMatricula())
                .unidadeCodigo(usuario.getUnidadeLotacao() != null ? usuario.getUnidadeLotacao().getCodigo() : null)
                .build();
    }

    private UnidadeDto toUnidadeDto(Unidade unidade) {
        Unidade superior = unidade.getUnidadeSuperior();
        return UnidadeDto.builder()
                .codigo(unidade.getCodigo())
                .nome(unidade.getNome())
                .sigla(unidade.getSigla())
                .codigoPai(superior != null ? superior.getCodigo() : null)
                .tipo(unidade.getTipo().name())
                .isElegivel(unidade.getTipo() != TipoUnidade.INTERMEDIARIA)
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

    private ResponsavelDto montarResponsavelDto(Long unidadeCodigo, List<Usuario> chefes) {
        Usuario titular = chefes.getFirst();
        Usuario substituto = chefes.size() > 1 ? chefes.get(1) : null;

        return ResponsavelDto.builder()
                .unidadeCodigo(unidadeCodigo)
                .titularTitulo(titular.getTituloEleitoral())
                .titularNome(titular.getNome())
                .substitutoTitulo(substituto != null ? substituto.getTituloEleitoral() : null)
                .substitutoNome(substituto != null ? substituto.getNome() : null)
                .build();
    }

    // ===================== ADMINISTRADORES =====================

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
        Usuario usuario = usuarioRepo.findById(usuarioTitulo)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada(ENTIDADE_USUARIO, usuarioTitulo));

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
        log.info("Removendo administrador: {}", usuarioTitulo);

        if (!administradorRepo.existsById(usuarioTitulo)) {
            throw new ErroValidacao("Usuário não é administrador");
        }

        if (usuarioTitulo.equals(usuarioAtualTitulo)) {
            throw new ErroValidacao("Não é permitido remover a si mesmo como administrador");
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
        if (usuario == null)
            return null;

        Unidade unidadeLotacao = usuario.getUnidadeLotacao();

        return AdministradorDto.builder()
                .tituloEleitoral(usuario.getTituloEleitoral())
                .nome(usuario.getNome())
                .matricula(usuario.getMatricula())
                .unidadeCodigo(unidadeLotacao != null ? unidadeLotacao.getCodigo() : null)
                .unidadeSigla(unidadeLotacao != null ? unidadeLotacao.getSigla() : null)
                .build();
    }

    public @Nullable String extractTituloUsuario(@Nullable Object principal) {
        if (principal instanceof String string)
            return string;
        if (principal instanceof Usuario usuario)
            return usuario.getTituloEleitoral();
        return principal != null ? principal.toString() : null;
    }
}
