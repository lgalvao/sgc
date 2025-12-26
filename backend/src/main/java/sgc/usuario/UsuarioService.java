package sgc.usuario;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.seguranca.AcessoAdClient;
import sgc.seguranca.GerenciadorJwt;
import sgc.seguranca.dto.EntrarReq;
import sgc.seguranca.dto.PerfilUnidade;
import sgc.comum.erros.ErroAccessoNegado;
import sgc.comum.erros.ErroAutenticacao;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.unidade.dto.UnidadeDto;
import sgc.usuario.dto.*;
import sgc.usuario.model.Perfil;
import sgc.usuario.model.Usuario;
import sgc.usuario.model.UsuarioPerfil;
import sgc.usuario.model.UsuarioPerfilRepo;
import sgc.usuario.model.UsuarioRepo;
import sgc.unidade.model.TipoUnidade;
import sgc.unidade.model.Unidade;
import sgc.unidade.model.UnidadeRepo;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

@Service
@Slf4j
public class UsuarioService {
    private final UnidadeRepo unidadeRepo;
    private final UsuarioRepo usuarioRepo;
    private final UsuarioPerfilRepo usuarioPerfilRepo;
    private final GerenciadorJwt gerenciadorJwt;
    private final AcessoAdClient acessoAdClient;

    @Value("${aplicacao.ambiente-testes:false}")
    private boolean ambienteTestes;

    // SENTINEL: Cache para controlar autenticações recentes e prevenir bypass
    private final Map<String, java.time.LocalDateTime> autenticacoesRecentes = new java.util.concurrent.ConcurrentHashMap<>();

    @Transactional(readOnly = true)
    public Usuario carregarUsuarioParaAutenticacao(String titulo) {
        Usuario usuario = usuarioRepo.findById(titulo).orElse(null);
        if (usuario != null) {
            carregarAtribuicoes(usuario);
            // Inicializa a coleção lazy
            if (usuario.getAtribuicoesTemporarias() != null) {
                Hibernate.initialize(usuario.getAtribuicoesTemporarias());
            }
            // Força a inicialização das authorities
            usuario.getAuthorities();
        }
        return usuario;
    }

    @Autowired
    public UsuarioService(UnidadeRepo unidadeRepo,
                       UsuarioRepo usuarioRepo,
                       UsuarioPerfilRepo usuarioPerfilRepo,
                       GerenciadorJwt gerenciadorJwt,
                       @Autowired(required = false) AcessoAdClient acessoAdClient) {
        this.unidadeRepo = unidadeRepo;
        this.usuarioRepo = usuarioRepo;
        this.usuarioPerfilRepo = usuarioPerfilRepo;
        this.gerenciadorJwt = gerenciadorJwt;
        this.acessoAdClient = acessoAdClient;
    }

    public Optional<UsuarioDto> buscarUsuarioPorTitulo(String titulo) {
        return usuarioRepo.findById(titulo).map(this::toUsuarioDto);
    }

    @Transactional(readOnly = true)
    public Usuario buscarUsuarioPorLogin(String login) {
        Usuario usuario = usuarioRepo
                .findById(login)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Usuário", login));

        carregarAtribuicoes(usuario);
        // Inicializa a coleção lazy para evitar LazyInitializationException
        if (usuario.getAtribuicoesTemporarias() != null) {
            Hibernate.initialize(usuario.getAtribuicoesTemporarias());
        }
        return usuario;
    }

    @Transactional(readOnly = true)
    public Usuario buscarResponsavelVigente(String sigla) {
        Unidade unidade = unidadeRepo
                .findBySigla(sigla)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Unidade", sigla));

        Usuario usuario = usuarioRepo
                .chefePorCodUnidade(unidade.getCodigo())
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Responsável da unidade", sigla));
        
        carregarAtribuicoes(usuario);
        // Inicializa a coleção lazy para evitar LazyInitializationException
        if (usuario.getAtribuicoesTemporarias() != null) {
            Hibernate.initialize(usuario.getAtribuicoesTemporarias());
        }
        return usuario;
    }

    @Transactional(readOnly = true)
    public List<PerfilDto> buscarPerfisUsuario(String titulo) {
        return usuarioRepo
                .findById(titulo)
                .map(usuario -> {
                    carregarAtribuicoes(usuario);
                    // Inicializa a coleção lazy para evitar LazyInitializationException
                    if (usuario.getAtribuicoesTemporarias() != null) {
                        Hibernate.initialize(usuario.getAtribuicoesTemporarias());
                    }
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

    public Optional<UsuarioDto> buscarUsuarioPorEmail(String email) {
        return usuarioRepo.findByEmail(email).map(this::toUsuarioDto);
    }

    public List<UsuarioDto> buscarUsuariosAtivos() {
        return usuarioRepo.findAll().stream().map(this::toUsuarioDto).toList();
    }

    public Optional<UnidadeDto> buscarUnidadePorCodigo(Long codigo) {
        return unidadeRepo.findById(codigo).map(this::toUnidadeDto);
    }

    public Optional<UnidadeDto> buscarUnidadePorSigla(String sigla) {
        return unidadeRepo.findBySigla(sigla).map(this::toUnidadeDto);
    }

    public List<UnidadeDto> buscarUnidadesAtivas() {
        return unidadeRepo.findAll().stream().map(this::toUnidadeDto).toList();
    }

    public List<UnidadeDto> buscarSubunidades(Long codigoPai) {
        return unidadeRepo.findByUnidadeSuperiorCodigo(codigoPai).stream()
                .map(this::toUnidadeDto)
                .toList();
    }

    public List<UnidadeDto> construirArvoreHierarquica() {
        List<Unidade> todas = unidadeRepo.findAll();
        Map<Long, List<UnidadeDto>> subunidadesPorPai = new HashMap<>();
        Map<Long, UnidadeDto> dtoMap = new HashMap<>();

        // Primeiro cria todos os DTOs
        for (Unidade u : todas) {
            dtoMap.put(u.getCodigo(), toUnidadeDto(u));
        }

        // Organiza a hierarquia
        for (Unidade u : todas) {
            if (u.getUnidadeSuperior() != null) {
                subunidadesPorPai
                        .computeIfAbsent(u.getUnidadeSuperior().getCodigo(), k -> new ArrayList<>())
                        .add(dtoMap.get(u.getCodigo()));
            }
        }

        // Monta a árvore recursivamente (ou apenas associa os filhos já que temos o mapa)
        for (UnidadeDto dto : dtoMap.values()) {
            List<UnidadeDto> filhos = subunidadesPorPai.get(dto.getCodigo());
            if (filhos != null) {
                dto.setSubunidades(filhos);
            }
        }

        // Retorna apenas as raízes
        return todas.stream()
                .filter(u -> u.getUnidadeSuperior() == null)
                .map(u -> dtoMap.get(u.getCodigo()))
                .toList();
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

        todosChefes.forEach(usuario -> {
            carregarAtribuicoes(usuario);
            // Inicializa a coleção lazy para evitar LazyInitializationException
            if (usuario.getAtribuicoesTemporarias() != null) {
                Hibernate.initialize(usuario.getAtribuicoesTemporarias());
            }
        });

        Map<Long, List<Usuario>> chefesPorUnidade = todosChefes.stream()
                .flatMap(u -> u.getTodasAtribuicoes().stream()
                        .filter(a -> a.getPerfil() == Perfil.CHEFE
                                && unidadesCodigos.contains(
                                a.getUnidadeCodigo()))
                        .map(a -> new AbstractMap.SimpleEntry<>(a.getUnidadeCodigo(), u)))
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toList()))
                );

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
                .findById(titulo)
                .map(u -> {
                    carregarAtribuicoes(u);
                    // Inicializa a coleção lazy para evitar LazyInitializationException
                    if (u.getAtribuicoesTemporarias() != null) {
                        Hibernate.initialize(u.getAtribuicoesTemporarias());
                    }
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
                .findById(titulo)
                .map(u -> {
                    carregarAtribuicoes(u);
                    // Inicializa a coleção lazy para evitar LazyInitializationException
                    if (u.getAtribuicoesTemporarias() != null) {
                        Hibernate.initialize(u.getAtribuicoesTemporarias());
                    }
                    return u.getTodasAtribuicoes().stream()
                            .anyMatch(a -> a.getPerfil().name().equals(perfil)
                                    && a.getUnidadeCodigo().equals(unidadeCodigo));
                })
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public List<Long> buscarUnidadesPorPerfil(String titulo, String perfil) {
        return usuarioRepo
                .findById(titulo)
                .map(u -> {
                    carregarAtribuicoes(u);
                    // Inicializa a coleção lazy para evitar LazyInitializationException
                    if (u.getAtribuicoesTemporarias() != null) {
                        Hibernate.initialize(u.getAtribuicoesTemporarias());
                    }
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
        return UnidadeDto.builder()
                .codigo(unidade.getCodigo())
                .nome(unidade.getNome())
                .sigla(unidade.getSigla())
                .codigoPai(unidade.getUnidadeSuperior() != null
                        ? unidade.getUnidadeSuperior().getCodigo()
                        : null)
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

    public boolean autenticar(String tituloEleitoral, String senha) {
        log.debug("Autenticando usuário no AD: {}", tituloEleitoral);

        boolean autenticado = false;
        if (acessoAdClient == null) {
            if (ambienteTestes) {
                log.debug("Ambiente de testes: Simulando autenticação com sucesso.");
                autenticado = true;
            } else {
                log.error("ERRO CRÍTICO DE SEGURANÇA: Tentativa de autenticação sem provedor configurado em ambiente produtivo. Usuário: {}", tituloEleitoral);
                autenticado = false;
            }
        } else {
            try {
                autenticado = acessoAdClient.autenticar(tituloEleitoral, senha);
            } catch (ErroAutenticacao e) {
                log.warn("Falha na autenticação do usuário {}: {}", tituloEleitoral, e.getMessage());
                autenticado = false;
            }
        }

        if (autenticado) {
            autenticacoesRecentes.put(tituloEleitoral, java.time.LocalDateTime.now());
        }
        return autenticado;
    }

    @Transactional(readOnly = true)
    public List<PerfilUnidade> autorizar(String tituloEleitoral) {
        // SENTINEL: Previne Information Disclosure verificando se usuário se autenticou recentemente
        if (!autenticacoesRecentes.containsKey(tituloEleitoral)) {
            log.warn("Tentativa de autorização sem autenticação prévia para usuário {}", tituloEleitoral);
            throw new ErroAutenticacao("É necessário autenticar-se antes de consultar autorizações.");
        }

        return buscarAutorizacoesInterno(tituloEleitoral);
    }

    /**
     * Método interno para buscar autorizações sem verificação de autenticação prévia.
     * Usado internamente pelo método entrar() para evitar chamada transacional via 'this'.
     */
    private List<PerfilUnidade> buscarAutorizacoesInterno(String tituloEleitoral) {
        log.debug("Buscando autorizações (perfis e unidades) para o usuário: {}", tituloEleitoral);
        Usuario usuario = usuarioRepo
                .findById(tituloEleitoral)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Usuário", tituloEleitoral));

        carregarAtribuicoes(usuario);
        // Inicializa a coleção lazy para evitar LazyInitializationException
        if (usuario.getAtribuicoesTemporarias() != null) {
            Hibernate.initialize(usuario.getAtribuicoesTemporarias());
        }

        return usuario.getTodasAtribuicoes().stream().map(atribuicao -> new PerfilUnidade(
                        atribuicao.getPerfil(),
                        toUnidadeDto(atribuicao.getUnidade())))
                .toList();
    }

    public void entrar(String tituloEleitoral, @NonNull PerfilUnidade pu) {
        log.debug("Usuário {} entrou. Perfil: {}, Unidade: {}",
                tituloEleitoral,
                pu.getPerfil(),
                pu.getSiglaUnidade());
    }

    @Transactional(readOnly = true)
    public String entrar(@NonNull EntrarReq request) {
        // SENTINEL: Verifica se houve autenticação recente (previne bypass chamando /entrar direto)
        java.time.LocalDateTime ultimoAcesso = autenticacoesRecentes.get(request.getTituloEleitoral());

        if (ultimoAcesso == null || ultimoAcesso.isBefore(java.time.LocalDateTime.now().minusMinutes(5))) {
            log.warn("Tentativa de acesso não autorizada (sem login prévio) para usuário {}", request.getTituloEleitoral());
            throw new ErroAutenticacao("Sessão de login expirada ou inválida. Por favor, autentique-se novamente.");
        }

        Long codUnidade = request.getUnidadeCodigo();

        if (!unidadeRepo.existsById(codUnidade)) {
            throw new ErroEntidadeNaoEncontrada("Unidade", codUnidade);
        }

        List<PerfilUnidade> autorizacoes = buscarAutorizacoesInterno(request.getTituloEleitoral());
        
        // Remove a autenticação do cache após usá-la (garante que só pode entrar uma vez por autenticação)
        autenticacoesRecentes.remove(request.getTituloEleitoral());
        boolean autorizado = autorizacoes
                .stream()
                .anyMatch(pu -> {
                    Perfil perfil = pu.getPerfil();
                    return perfil.name().equals(request.getPerfil())
                            && pu.getUnidade().getCodigo().equals(codUnidade);
                });

        if (!autorizado) {
            throw new ErroAccessoNegado("Usuário não tem permissão para acessar com perfil e unidade informados.");
        }

        String token = gerenciadorJwt.gerarToken(
                request.getTituloEleitoral(),
                Perfil.valueOf(request.getPerfil()),
                codUnidade
        );

        log.debug("Usuário {} entrou com sucesso. JWT gerado.", request.getTituloEleitoral());
        return token;
    }
}
