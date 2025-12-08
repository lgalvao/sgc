package sgc.sgrh.service;

import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.sgrh.dto.PerfilDto;
import sgc.sgrh.dto.ResponsavelDto;
import sgc.sgrh.dto.UnidadeDto;
import sgc.sgrh.dto.UsuarioDto;
import sgc.sgrh.model.Perfil;
import sgc.sgrh.model.Usuario;
import sgc.sgrh.model.UsuarioPerfil;
import sgc.sgrh.model.UsuarioRepo;
import sgc.unidade.model.Unidade;
import sgc.unidade.model.UnidadeRepo;

@Service
@Slf4j
@RequiredArgsConstructor
public class SgrhService {
    private final UnidadeRepo unidadeRepo;
    private final UsuarioRepo usuarioRepo;

    public Optional<UsuarioDto> buscarUsuarioPorTitulo(String titulo) {
        return usuarioRepo.findById(titulo).map(this::toUsuarioDto);
    }

    public Usuario buscarUsuarioPorLogin(String login) {
        return usuarioRepo
                .findById(login)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Usuário", login));
    }

    public Usuario buscarResponsavelVigente(String sigla) {
        log.debug("Buscando responsável vigente para a sigla {}.", sigla);
        Unidade unidade =
                unidadeRepo
                        .findBySigla(sigla)
                        .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Unidade", sigla));

        return usuarioRepo
                .findChefeByUnidadeCodigo(unidade.getCodigo())
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Responsável da unidade", sigla));
    }

    public List<PerfilDto> buscarPerfisUsuario(String titulo) {
        return usuarioRepo
                .findById(titulo)
                .map(
                        usuario ->
                                usuario.getTodasAtribuicoes().stream()
                                        .map(this::toPerfilDto)
                                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }

    public Optional<UsuarioDto> buscarUsuarioPorEmail(String email) {
        return usuarioRepo.findByEmail(email).map(this::toUsuarioDto);
    }

    public List<UsuarioDto> buscarUsuariosAtivos() {
        return usuarioRepo.findAll().stream().map(this::toUsuarioDto).collect(Collectors.toList());
    }

    public Optional<UnidadeDto> buscarUnidadePorCodigo(Long codigo) {
        return unidadeRepo.findById(codigo).map(this::toUnidadeDto);
    }

    public Optional<UnidadeDto> buscarUnidadePorSigla(String sigla) {
        return unidadeRepo.findBySigla(sigla).map(this::toUnidadeDto);
    }

    public List<UnidadeDto> buscarUnidadesAtivas() {
        return unidadeRepo.findAll().stream().map(this::toUnidadeDto).collect(Collectors.toList());
    }

    public List<UnidadeDto> buscarSubunidades(Long codigoPai) {
        return unidadeRepo.findByUnidadeSuperiorCodigo(codigoPai).stream()
                .map(this::toUnidadeDto)
                .collect(Collectors.toList());
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

        // Monta a árvore recursivamente (ou apenas associa os filhos já que temos o
        // mapa)
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
                .collect(Collectors.toList());
    }

    public Optional<ResponsavelDto> buscarResponsavelUnidade(Long unidadeCodigo) {
        List<Usuario> chefes = usuarioRepo.findChefesByUnidadesCodigos(List.of(unidadeCodigo));
        if (chefes.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(montarResponsavelDto(unidadeCodigo, chefes));
    }

    public Map<Long, ResponsavelDto> buscarResponsaveisUnidades(List<Long> unidadesCodigos) {
        List<Usuario> todosChefes = usuarioRepo.findChefesByUnidadesCodigos(unidadesCodigos);

        Map<Long, List<Usuario>> chefesPorUnidade =
                todosChefes.stream()
                        .flatMap(
                                u ->
                                        u.getTodasAtribuicoes().stream()
                                                .filter(
                                                        a ->
                                                                a.getPerfil() == Perfil.CHEFE
                                                                        && unidadesCodigos.contains(
                                                                                a.getUnidade()
                                                                                        .getCodigo()))
                                                .map(
                                                        a ->
                                                                new AbstractMap.SimpleEntry<>(
                                                                        a.getUnidade().getCodigo(),
                                                                        u)))
                        .collect(
                                Collectors.groupingBy(
                                        Map.Entry::getKey,
                                        Collectors.mapping(
                                                Map.Entry::getValue, Collectors.toList())));

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
                .collect(Collectors.toMap(Usuario::getTituloEleitoral, this::toUsuarioDto));
    }

    public List<Long> buscarUnidadesOndeEhResponsavel(String titulo) {
        return usuarioRepo
                .findById(titulo)
                .map(
                        u ->
                                u.getTodasAtribuicoes().stream()
                                        .filter(a -> a.getPerfil() == Perfil.CHEFE)
                                        .map(a -> a.getUnidade().getCodigo())
                                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }

    public boolean usuarioTemPerfil(String titulo, String perfil, Long unidadeCodigo) {
        return usuarioRepo
                .findById(titulo)
                .map(
                        u ->
                                u.getTodasAtribuicoes().stream()
                                        .anyMatch(
                                                a ->
                                                        a.getPerfil().name().equals(perfil)
                                                                && a.getUnidade()
                                                                        .getCodigo()
                                                                        .equals(unidadeCodigo)))
                .orElse(false);
    }

    public List<Long> buscarUnidadesPorPerfil(String titulo, String perfil) {
        return usuarioRepo
                .findById(titulo)
                .map(
                        u ->
                                u.getTodasAtribuicoes().stream()
                                        .filter(a -> a.getPerfil().name().equals(perfil))
                                        .map(a -> a.getUnidade().getCodigo())
                                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }

    private UsuarioDto toUsuarioDto(Usuario usuario) {
        return UsuarioDto.builder()
                .titulo(usuario.getTituloEleitoral())
                .nome(usuario.getNome())
                .email(usuario.getEmail())
                .matricula(
                        "MAT" + usuario.getTituloEleitoral()) // Simulação de matrícula baseada no
                // título
                .build();
    }


    private UnidadeDto toUnidadeDto(Unidade unidade) {
        return UnidadeDto.builder()
                .codigo(unidade.getCodigo())
                .nome(unidade.getNome())
                .sigla(unidade.getSigla())
                .codigoPai(
                        unidade.getUnidadeSuperior() != null
                                ? unidade.getUnidadeSuperior().getCodigo()
                                : null)
                .tipo(unidade.getTipo().name())
                .isElegivel(false) // Default
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
        Usuario titular = chefes.get(0);
        Usuario substituto = chefes.size() > 1 ? chefes.get(1) : null;

        return ResponsavelDto.builder()
                .unidadeCodigo(unidadeCodigo)
                .titularTitulo(titular.getTituloEleitoral())
                .titularNome(titular.getNome())
                .substitutoTitulo(substituto != null ? substituto.getTituloEleitoral() : null)
                .substitutoNome(substituto != null ? substituto.getNome() : null)
                .build();
    }
}
