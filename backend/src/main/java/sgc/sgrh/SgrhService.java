package sgc.sgrh;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sgc.comum.erros.ErroAccessoNegado;
import sgc.comum.erros.ErroAutenticacao;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.sgrh.dto.*;
import sgc.sgrh.model.Perfil;
import sgc.sgrh.model.Usuario;
import sgc.sgrh.model.UsuarioPerfil;
import sgc.sgrh.model.UsuarioRepo;
import sgc.unidade.model.TipoUnidade;
import sgc.unidade.model.Unidade;
import sgc.unidade.model.UnidadeRepo;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

@Service
@Slf4j
public class SgrhService {
    private final UnidadeRepo unidadeRepo;
    private final UsuarioRepo usuarioRepo;
    private final sgc.sgrh.model.UsuarioPerfilRepo usuarioPerfilRepo;
    private final sgc.sgrh.autenticacao.GerenciadorJwt gerenciadorJwt;

    @Autowired(required = false)
    private sgc.sgrh.autenticacao.AcessoAdClient acessoAdClient;

    public SgrhService(UnidadeRepo unidadeRepo,
                       UsuarioRepo usuarioRepo,
                       sgc.sgrh.model.UsuarioPerfilRepo usuarioPerfilRepo,
                       sgc.sgrh.autenticacao.GerenciadorJwt gerenciadorJwt) {
        this.unidadeRepo = unidadeRepo;
        this.usuarioRepo = usuarioRepo;
        this.usuarioPerfilRepo = usuarioPerfilRepo;
        this.gerenciadorJwt = gerenciadorJwt;
    }

    public Optional<UsuarioDto> buscarUsuarioPorTitulo(String titulo) {
        return usuarioRepo.findById(titulo).map(this::toUsuarioDto);
    }

    public Usuario buscarUsuarioPorLogin(String login) {
        Usuario usuario = usuarioRepo
                .findById(login)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Usuário", login));

        carregarAtribuicoes(usuario);
        return usuario;
    }

    public Usuario buscarResponsavelVigente(String sigla) {
        Unidade unidade = unidadeRepo
                .findBySigla(sigla)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Unidade", sigla));

        return usuarioRepo
                .chefePorCodUnidade(unidade.getCodigo())
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Responsável da unidade", sigla));
    }

    public List<PerfilDto> buscarPerfisUsuario(String titulo) {
        return usuarioRepo
                .findById(titulo)
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

    public Map<Long, ResponsavelDto> buscarResponsaveisUnidades(List<Long> unidadesCodigos) {
        List<Usuario> todosChefes = usuarioRepo.findChefesByUnidadesCodigos(unidadesCodigos);

        todosChefes.forEach(this::carregarAtribuicoes);

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

    public List<Long> buscarUnidadesOndeEhResponsavel(String titulo) {
        return usuarioRepo
                .findById(titulo)
                .map(u -> {
                    carregarAtribuicoes(u);
                    return u.getTodasAtribuicoes().stream()
                            .filter(a -> a.getPerfil() == Perfil.CHEFE)
                            .map(UsuarioPerfil::getUnidadeCodigo)
                            .toList();
                })
                .orElse(Collections.emptyList());
    }

    public boolean usuarioTemPerfil(String titulo, String perfil, Long unidadeCodigo) {
        return usuarioRepo
                .findById(titulo)
                .map(u -> {
                    carregarAtribuicoes(u);
                    return u.getTodasAtribuicoes().stream()
                            .anyMatch(a -> a.getPerfil().name().equals(perfil)
                                    && a.getUnidadeCodigo().equals(unidadeCodigo));
                })
                .orElse(false);
    }

    public List<Long> buscarUnidadesPorPerfil(String titulo, String perfil) {
        return usuarioRepo
                .findById(titulo)
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

        if (acessoAdClient == null) {
            log.debug("AcessoAdClient não disponível (profile test/e2e). Simulando autenticação.");
            return true;
        }

        try {
            return acessoAdClient.autenticar(tituloEleitoral, senha);
        } catch (ErroAutenticacao e) {
            log.warn("Falha na autenticação do usuário {}: {}", tituloEleitoral, e.getMessage());
            return false;
        }
    }

    public List<PerfilUnidade> autorizar(String tituloEleitoral) {
        log.debug("Buscando autorizações (perfis e unidades) para o usuário: {}", tituloEleitoral);
        Usuario usuario = usuarioRepo
                .findById(tituloEleitoral)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Usuário", tituloEleitoral));

        carregarAtribuicoes(usuario);

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

    public String entrar(@NonNull EntrarReq request) {
        Long codUnidade = request.getUnidadeCodigo();

        if (!unidadeRepo.existsById(codUnidade)) {
            throw new ErroEntidadeNaoEncontrada("Unidade", codUnidade);
        }

        List<PerfilUnidade> autorizacoes = autorizar(request.getTituloEleitoral());
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
