package sgc.organizacao;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.comum.erros.ErroValidacao;
import sgc.comum.repo.RepositorioComum;
import sgc.organizacao.dto.AtribuicaoTemporariaDto;
import sgc.organizacao.dto.CriarAtribuicaoTemporariaRequest;
import sgc.organizacao.dto.ResponsavelDto;
import sgc.organizacao.dto.UnidadeDto;
import sgc.organizacao.dto.UsuarioDto;
import sgc.organizacao.mapper.UsuarioMapper;
import sgc.organizacao.model.*;

import static java.util.stream.Collectors.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class UnidadeFacade {
    private static final String MSG_NAO_ENCONTRADA = " não encontrada";

    private final UnidadeRepo unidadeRepo;
    private final sgc.organizacao.model.UnidadeMapaRepo unidadeMapaRepo;
    private final UsuarioRepo usuarioRepo;
    private final UsuarioPerfilRepo usuarioPerfilRepo;
    private final AtribuicaoTemporariaRepo atribuicaoTemporariaRepo;
    private final UsuarioMapper usuarioMapper;
    private final RepositorioComum repo;

    public List<UnidadeDto> buscarArvoreHierarquica() {
        List<Unidade> todasUnidades = unidadeRepo.findAllWithHierarquia();
        return montarHierarquia(todasUnidades);
    }

    public List<UnidadeDto> buscarTodasUnidades() {
        return buscarArvoreHierarquica();
    }

    public List<AtribuicaoTemporariaDto> buscarTodasAtribuicoes() {
        return atribuicaoTemporariaRepo.findAll().stream()
                .map(usuarioMapper::toAtribuicaoTemporariaDto)
                .toList();
    }

    public List<UnidadeDto> buscarArvoreComElegibilidade(
            boolean requerMapaVigente, java.util.Set<Long> unidadesBloqueadas) {
        List<Unidade> todasUnidades = unidadeRepo.findAllWithHierarquia();

        return montarHierarquiaComElegibilidade(todasUnidades, requerMapaVigente, unidadesBloqueadas);
    }

    private List<UnidadeDto> montarHierarquiaComElegibilidade(
            List<Unidade> unidades, boolean requerMapaVigente, Set<Long> unidadesBloqueadas) {

        Set<Long> unidadesComMapa = requerMapaVigente
                ? new HashSet<>(unidadeMapaRepo.findAllUnidadeCodigos())
                : Collections.emptySet();

        Function<Unidade, Boolean> elegibilidadeChecker = u ->
                u.getTipo() != sgc.organizacao.model.TipoUnidade.INTERMEDIARIA
                        && (!requerMapaVigente || unidadesComMapa.contains(u.getCodigo()))
                        && !unidadesBloqueadas.contains(u.getCodigo());

        return montarHierarquia(unidades, elegibilidadeChecker);
    }


    public void criarAtribuicaoTemporaria(Long codUnidade, CriarAtribuicaoTemporariaRequest request) {
        Unidade unidade = repo.buscar(Unidade.class, codUnidade);

        String titulo = request.tituloEleitoralUsuario();
        Usuario usuario = repo.buscar(Usuario.class, titulo);

        LocalDate inicio = request.dataInicio() != null ? request.dataInicio() : LocalDate.now();

        if (request.dataTermino().isBefore(inicio)) {
            throw new ErroValidacao("A data de término deve ser posterior à data de início.");
        }

        AtribuicaoTemporaria atribuicao = new AtribuicaoTemporaria()
                .setUnidade(unidade)
                .setUsuarioTitulo(usuario.getTituloEleitoral())
                .setUsuarioMatricula(usuario.getMatricula())
                .setDataInicio(request.dataInicio() != null ? request.dataInicio().atStartOfDay() : LocalDateTime.now())
                .setDataTermino(request.dataTermino().atTime(23, 59, 59))
                .setJustificativa(request.justificativa());

        atribuicaoTemporariaRepo.save(atribuicao);
    }

    public boolean verificarMapaVigente(Long codigoUnidade) {
        return unidadeMapaRepo.existsById(codigoUnidade);
    }

    public List<UsuarioDto> buscarUsuariosPorUnidade(Long codigoUnidade) {
        return usuarioRepo.findByUnidadeLotacaoCodigo(codigoUnidade).stream()
                .map(usuarioMapper::toUsuarioDto)
                .toList();
    }

    private List<UnidadeDto> montarHierarquia(List<Unidade> unidades) {
        return montarHierarquia(unidades, null);
    }

    private List<UnidadeDto> montarHierarquia(List<Unidade> unidades, @Nullable Function<Unidade, Boolean> elegibilidadeChecker) {
        Map<Long, UnidadeDto> mapaUnidades = new HashMap<>();
        Map<Long, List<UnidadeDto>> mapaFilhas = new HashMap<>();
        List<UnidadeDto> raizes = new ArrayList<>();

        for (Unidade u : unidades) {
            boolean isElegivel = elegibilidadeChecker != null ? elegibilidadeChecker.apply(u) : true;
            UnidadeDto dto = usuarioMapper.toUnidadeDto(u, isElegivel);

            mapaUnidades.put(u.getCodigo(), dto);
            mapaFilhas.putIfAbsent(u.getCodigo(), new ArrayList<>());
        }

        for (Unidade u : unidades) {
            UnidadeDto dto = mapaUnidades.get(u.getCodigo());

            Optional.ofNullable(u.getUnidadeSuperior()).ifPresentOrElse(
                    superior -> {
                        Long codigoPai = superior.getCodigo();
                        mapaFilhas.computeIfAbsent(codigoPai, k -> new ArrayList<>()).add(dto);
                    },
                    () -> raizes.add(dto)
            );
        }

        return raizes.stream().map(raiz -> montarComSubunidades(raiz, mapaFilhas)).toList();
    }

    private UnidadeDto montarComSubunidades(UnidadeDto dto, Map<Long, List<UnidadeDto>> mapaFilhas) {
        List<UnidadeDto> filhas = mapaFilhas.get(dto.getCodigo());
        if (filhas == null || filhas.isEmpty()) {
            return dto;
        }

        List<UnidadeDto> subunidadesCompletas = filhas.stream()
                .map(filha -> montarComSubunidades(filha, mapaFilhas))
                .toList();

        dto.setSubunidades(subunidadesCompletas);

        return dto;
    }

    public UnidadeDto buscarPorSigla(String sigla) {
        Unidade unidade = buscarEntidadePorSigla(sigla);
        return usuarioMapper.toUnidadeDto(unidade, false);
    }

    public Unidade buscarEntidadePorSigla(String sigla) {
        return unidadeRepo
                .findBySigla(sigla)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Unidade com sigla %s%s".formatted(sigla, MSG_NAO_ENCONTRADA)));
    }

    public UnidadeDto buscarPorCodigo(Long codigo) {
        Unidade unidade = buscarEntidadePorId(codigo);
        return usuarioMapper.toUnidadeDto(unidade, false);
    }

    public List<UnidadeDto> buscarSubordinadas(Long codUnidade) {
        return unidadeRepo.findByUnidadeSuperiorCodigo(codUnidade).stream()
                .map(u -> usuarioMapper.toUnidadeDto(u, true))
                .toList();
    }

    public Unidade buscarEntidadePorId(Long codigo) {
        Unidade unidade = repo.buscar(Unidade.class, codigo);
        if (unidade.getSituacao() != SituacaoUnidade.ATIVA) {
            throw new ErroEntidadeNaoEncontrada("Unidade", codigo);
        }
        return unidade;
    }

    @Cacheable(value = "unidadeDescendentes", key = "#codigoUnidade")
    public List<Long> buscarIdsDescendentes(Long codigoUnidade) {
        List<Unidade> todas = buscarTodasEntidadesComHierarquia();

        Map<Long, List<Long>> mapPaiFilhos = new HashMap<>();
        for (Unidade u : todas) {
            Optional.ofNullable(u.getUnidadeSuperior()).ifPresent(superior ->
                    mapPaiFilhos.computeIfAbsent(superior.getCodigo(), k -> new ArrayList<>()).add(u.getCodigo())
            );
        }

        List<Long> descendentes = new ArrayList<>();
        coletarDescendentes(codigoUnidade, mapPaiFilhos, descendentes);
        return descendentes;
    }

    private void coletarDescendentes(Long atual, Map<Long, List<Long>> map, List<Long> resultado) {
        List<Long> filhos = map.get(atual);
        if (filhos != null) {
            for (Long filho : filhos) {
                resultado.add(filho);
                coletarDescendentes(filho, map, resultado);
            }
        }
    }

    public List<Unidade> buscarEntidadesPorIds(List<Long> codigos) {
        return unidadeRepo.findAllById(codigos);
    }

    @Cacheable(value = "arvoreUnidades", unless = "#result == null || #result.isEmpty()")
    public List<Unidade> buscarTodasEntidadesComHierarquia() {
        return unidadeRepo.findAllWithHierarquia();
    }

    public List<String> buscarSiglasPorIds(List<Long> codigos) {
        return unidadeRepo.findSiglasByCodigos(codigos);
    }

    public boolean verificarExistenciaMapaVigente(Long codigoUnidade) {
        return unidadeMapaRepo.existsById(codigoUnidade);
    }

    @Transactional
    public void definirMapaVigente(Long codigoUnidade, sgc.mapa.model.Mapa mapa) {
        sgc.organizacao.model.UnidadeMapa unidadeMapa = unidadeMapaRepo.findById(codigoUnidade)
                .orElse(new sgc.organizacao.model.UnidadeMapa());
        unidadeMapa.setUnidadeCodigo(codigoUnidade);
        unidadeMapa.setMapaVigente(mapa);
        unidadeMapaRepo.save(unidadeMapa);
    }

    public UnidadeDto buscarArvore(Long codigo) {
        List<UnidadeDto> todas = buscarTodasUnidades();
        return buscarNaHierarquia(todas, codigo)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Unidade", codigo));
    }

    public List<String> buscarSiglasSubordinadas(String sigla) {
        List<UnidadeDto> todas = buscarTodasUnidades();
        return buscarNaHierarquiaPorSigla(todas, sigla)
                .map(raiz -> {
                    List<String> resultado = new ArrayList<>();
                    coletarSiglas(raiz, resultado);
                    return resultado;
                })
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Unidade", sigla));
    }

    public Optional<String> buscarSiglaSuperior(String sigla) {
        Unidade unidade = unidadeRepo
                .findBySigla(sigla)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Unidade com sigla %s%s".formatted(sigla, MSG_NAO_ENCONTRADA)));

        return Optional.ofNullable(unidade.getUnidadeSuperior()).map(Unidade::getSigla);
    }

    private Optional<UnidadeDto> buscarNaHierarquia(List<UnidadeDto> lista, Long codigo) {
        for (UnidadeDto u : lista) {
            if (u.getCodigo().equals(codigo)) {
                return Optional.of(u);
            }
            Optional<UnidadeDto> found = buscarNaHierarquia(u.getSubunidades(), codigo);
            if (found.isPresent()) {
                return found;
            }
        }
        return Optional.empty();
    }

    private Optional<UnidadeDto> buscarNaHierarquiaPorSigla(List<UnidadeDto> lista, String sigla) {
        for (UnidadeDto u : lista) {
            if (u.getSigla().equals(sigla)) {
                return Optional.of(u);
            }
            Optional<UnidadeDto> found = buscarNaHierarquiaPorSigla(u.getSubunidades(), sigla);
            if (found.isPresent()) {
                return found;
            }
        }
        return Optional.empty();
    }

    private void coletarSiglas(UnidadeDto unidade, List<String> resultado) {
        resultado.add(unidade.getSigla());
        unidade.getSubunidades().forEach(filha -> coletarSiglas(filha, resultado));
    }

    // ============ Métodos de Responsáveis de Unidade ============

    @Transactional(readOnly = true)
    public Usuario buscarResponsavelAtual(String siglaUnidade) {
        Unidade unidade = unidadeRepo.findBySigla(siglaUnidade)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Unidade", siglaUnidade));

        Usuario usuarioSimples = usuarioRepo
                .chefePorCodUnidade(unidade.getCodigo())
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Responsável da unidade", siglaUnidade));

        Usuario usuarioCompleto = usuarioRepo.findByIdWithAtribuicoes(usuarioSimples.getTituloEleitoral())
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Usuário", usuarioSimples.getTituloEleitoral()));

        carregarAtribuicoesUsuario(usuarioCompleto);
        return usuarioCompleto;
    }

    public ResponsavelDto buscarResponsavelUnidade(Long unidadeCodigo) {
        List<Usuario> chefes = usuarioRepo.findChefesByUnidadesCodigos(List.of(unidadeCodigo));
        if (chefes.isEmpty()) {
            throw new ErroEntidadeNaoEncontrada("Responsável da unidade", unidadeCodigo);
        }
        return montarResponsavelDto(unidadeCodigo, chefes);
    }

    @Transactional(readOnly = true)
    public Map<Long, ResponsavelDto> buscarResponsaveisUnidades(List<Long> unidadesCodigos) {
        if (unidadesCodigos.isEmpty()) return Collections.emptyMap();

        List<Usuario> todosChefes = usuarioRepo.findChefesByUnidadesCodigos(unidadesCodigos);
        if (todosChefes.isEmpty()) return Collections.emptyMap();

        List<String> titulos = todosChefes.stream().map(Usuario::getTituloEleitoral).toList();
        List<Usuario> chefesCompletos = usuarioRepo.findByIdInWithAtribuicoes(titulos);
        carregarAtribuicoesEmLote(chefesCompletos);

        Map<Long, List<Usuario>> chefesPorUnidade = chefesCompletos.stream()
                .flatMap(u -> u.getTodasAtribuicoes().stream()
                        .filter(a -> a.getPerfil() == Perfil.CHEFE && unidadesCodigos.contains(a.getUnidadeCodigo()))
                        .map(a -> new AbstractMap.SimpleEntry<>(a.getUnidadeCodigo(), u)))
                .collect(groupingBy(Map.Entry::getKey, mapping(Map.Entry::getValue, toList())));

        return chefesPorUnidade.entrySet().stream()
                .collect(toMap(
                        Map.Entry::getKey,
                        e -> montarResponsavelDto(e.getKey(), e.getValue())
                ));
    }

    // ============ Métodos auxiliares para Responsáveis ============

    private void carregarAtribuicoesUsuario(Usuario usuario) {
        List<UsuarioPerfil> atribuicoes = usuarioPerfilRepo.findByUsuarioTitulo(usuario.getTituloEleitoral());
        usuario.setAtribuicoes(new HashSet<>(atribuicoes));
    }

    private void carregarAtribuicoesEmLote(List<Usuario> usuarios) {
        if (usuarios.isEmpty()) return;

        List<String> titulos = usuarios.stream()
                .map(Usuario::getTituloEleitoral)
                .toList();

        List<UsuarioPerfil> todasAtribuicoes = usuarioPerfilRepo.findByUsuarioTituloIn(titulos);

        Map<String, Set<UsuarioPerfil>> atribuicoesPorUsuario = todasAtribuicoes.stream()
                .collect(groupingBy(UsuarioPerfil::getUsuarioTitulo, toSet()));

        for (Usuario usuario : usuarios) {
            Set<UsuarioPerfil> atribuicoes = atribuicoesPorUsuario.getOrDefault(
                    usuario.getTituloEleitoral(), new HashSet<>()
            );
            usuario.setAtribuicoes(atribuicoes);
        }
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
}
