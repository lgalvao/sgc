package sgc.organizacao.service;

import lombok.*;
import org.jspecify.annotations.*;
import org.springframework.beans.factory.*;
import org.springframework.cache.annotation.*;
import org.springframework.stereotype.*;
import sgc.comum.config.CacheConfig;
import sgc.comum.erros.*;
import sgc.organizacao.dto.*;
import sgc.organizacao.model.*;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;

import static sgc.organizacao.model.TipoUnidade.*;

/**
 * Serviço especializado para gerenciar a hierarquia de unidades organizacionais.
 */
@Service
@RequiredArgsConstructor
public class UnidadeHierarquiaService {
    private final UnidadeRepo unidadeRepo;
    private final UnidadeService unidadeService;
    private final CacheViewsOrganizacaoService cacheViewsOrganizacaoService;
    private final ObjectProvider<UnidadeHierarquiaService> selfProvider;
    private final sgc.organizacao.OrganizacaoDtoMapper organizacaoDtoMapper;

    /**
     * Busca a árvore hierárquica completa de unidades.
     */
    @Cacheable(cacheNames = CacheConfig.CACHE_ARVORE_UNIDADES, sync = true)
    public List<UnidadeDto> buscarArvoreHierarquica() {
        List<UnidadeHierarquiaLeitura> todasUnidades = cacheViewsOrganizacaoService.listarTodasUnidades();
        return montarHierarquia(todasUnidades, Map.of(), null);
    }

    /**
     * Busca a árvore hierárquica com filtro de elegibilidade.
     */
    public List<UnidadeDto> buscarArvoreComElegibilidade(Predicate<UnidadeElegibilidadeInfo> elegibilidadeChecker) {
        List<UnidadeHierarquiaLeitura> todasUnidades = cacheViewsOrganizacaoService.listarTodasUnidades();
        Map<Long, String> titulosResponsavel = carregarTitulosResponsavel(todasUnidades);
        return montarHierarquia(todasUnidades, titulosResponsavel, elegibilidadeChecker);
    }

    /**
     * Busca a árvore hierárquica com filtro de elegibilidade baseado em Configuraçãos de negócio.
     */
    public List<UnidadeDto> buscarArvoreComElegibilidade(
            boolean requerMapaVigente, Set<Long> unidadesBloqueadas) {

        Set<Long> unidadesComMapa = requerMapaVigente
                ? new HashSet<>(unidadeService.buscarTodosCodigosUnidadesComMapa())
                : Collections.emptySet();

        return buscarArvoreComElegibilidade(info ->
                info.tipo() != INTERMEDIARIA
                        && info.possuiResponsavelEfetivo()
                        && (!requerMapaVigente || unidadesComMapa.contains(info.codigo()))
                        && !unidadesBloqueadas.contains(info.codigo())
        );
    }

    /**
     * Busca todos os IDs de unidades descendentes de uma unidade.
     */
    public List<Long> buscarIdsDescendentes(Long codigoUnidade) {
        return buscarDescendentes(codigoUnidade, self().buscarMapaHierarquia());
    }

    /**
     * Busca todos os IDs de unidades descendentes usando um mapa pré-carregado.
     */
    public List<Long> buscarDescendentes(Long codigoUnidade, Map<Long, List<Long>> mapPaiFilhos) {
        List<Long> descendentes = new ArrayList<>();
        coletarDescendentes(codigoUnidade, mapPaiFilhos, descendentes);
        return descendentes;
    }

    /**
     * Constrói o mapa filho→pai (filhoCodigo → codigoPai) a partir de todas as unidades ativas.
     */
    @Cacheable(cacheNames = CacheConfig.CACHE_MAPA_FILHO_PAI, sync = true)
    public Map<Long, Long> buscarMapaFilhoPai() {
        List<UnidadeHierarquiaLeitura> todas = cacheViewsOrganizacaoService.listarTodasUnidades();
        Map<Long, Long> mapFilhoPai = new HashMap<>();
        for (UnidadeHierarquiaLeitura u : todas) {
            if (u.unidadeSuperiorCodigo() != null) {
                mapFilhoPai.put(u.codigo(), u.unidadeSuperiorCodigo());
            }
        }
        return mapFilhoPai;
    }

    /**
     * Retorna mapa de codigoUnidade para UnidadeHierarquiaLeitura para todas as unidades ativas.
     * Usa a mesma fonte cacheada de listarTodasUnidades, sem cache proprio.
     */
    public Map<Long, UnidadeHierarquiaLeitura> buscarMapaCodigoParaUnidade() {
        return cacheViewsOrganizacaoService.listarTodasUnidades().stream()
                .collect(Collectors.toMap(UnidadeHierarquiaLeitura::codigo, u -> u, (u1, u2) -> u1));
    }

    /**
     * Retorna a lista de códigos dos ancestores de uma unidade, do pai imediato até a raiz.
     */
    public List<Long> buscarCodigosSuperiores(Long codigoInicial) {
        Map<Long, Long> mapFilhoPai = self().buscarMapaFilhoPai();
        List<Long> superiores = new ArrayList<>();
        Set<Long> visitados = new HashSet<>();
        Long atual = mapFilhoPai.get(codigoInicial);
        while (atual != null && visitados.add(atual)) {
            superiores.add(atual);
            atual = mapFilhoPai.get(atual);
        }
        return superiores;
    }

    public @Nullable Long buscarCodigoPai(Long codigoFilho) {
        return self().buscarMapaFilhoPai().get(codigoFilho);
    }

    /**
     * Constrói o mapa de hierarquia (Pai -> Lista de Filhos) buscando todas as unidades.
     */
    @Cacheable(cacheNames = CacheConfig.CACHE_MAPA_HIERARQUIA_UNIDADES, sync = true)
    public Map<Long, List<Long>> buscarMapaHierarquia() {
        List<UnidadeHierarquiaLeitura> todas = cacheViewsOrganizacaoService.listarTodasUnidades();

        Map<Long, List<Long>> mapPaiFilhos = new HashMap<>();
        for (UnidadeHierarquiaLeitura u : todas) {
            Optional.ofNullable(u.unidadeSuperiorCodigo()).ifPresent(codigoPai ->
                    mapPaiFilhos.computeIfAbsent(codigoPai, k -> new ArrayList<>()).add(u.codigo())
            );
        }
        return mapPaiFilhos;
    }

    /**
     * Busca uma unidade específica na árvore hierárquica.
     *
     * @throws ErroEntidadeNaoEncontrada se a unidade não for encontrada
     */
    public UnidadeDto buscarArvore(Long codigo) {
        List<UnidadeDto> todas = self().buscarArvoreHierarquica();
        Optional<UnidadeDto> found = buscarNaHierarquia(todas, codigo);
        if (found.isPresent()) {
            return copiarComResponsavelAtual(found.get(), codigo);
        }
        Unidade u = unidadeService.buscarPorCodigo(codigo);
        return toUnidadeDtoObrigatoria(u);
    }

    /**
     * Busca todas as siglas de unidades subordinadas a uma unidade.
     *
     * @throws ErroEntidadeNaoEncontrada se a unidade não for encontrada
     */
    public List<String> buscarSiglasSubordinadas(String sigla) {
        List<UnidadeDto> todas = self().buscarArvoreHierarquica();
        Optional<UnidadeDto> found = buscarNaHierarquiaPorSigla(todas, sigla);

        if (found.isEmpty()) {
            unidadeService.buscarPorSigla(sigla);
            return List.of();
        }

        List<String> resultado = new ArrayList<>();
        coletarSiglas(found.get(), resultado);
        return resultado;
    }

    /**
     * Busca a sigla da unidade superior de uma unidade.
     *
     * @throws ErroEntidadeNaoEncontrada se a unidade não for encontrada
     */
    public Optional<String> buscarSiglaSuperior(String sigla) {
        Unidade unidade = unidadeService.buscarPorSigla(sigla);
        Long unidadeCodigo = unidade.getCodigo();
        Long codigoPai = buscarCodigoPai(unidadeCodigo);

        if (codigoPai == null) return Optional.empty();
        return unidadeRepo.buscarSiglaPorCodigo(codigoPai);
    }

    /**
     * Busca unidades subordinadas diretas de uma unidade.
     */
    public List<UnidadeDto> buscarSubordinadas(Long codUnidade) {
        return unidadeRepo.findByUnidadeSuperiorCodigo(codUnidade).stream()
                .map(this::toUnidadeDtoObrigatoria)
                .toList();
    }

    private Map<Long, String> carregarTitulosResponsavel(List<UnidadeHierarquiaLeitura> unidades) {
        List<Long> codigos = unidades.stream()
                .map(UnidadeHierarquiaLeitura::codigo)
                .toList();

        if (codigos.isEmpty()) {
            return Map.of();
        }

        Set<Long> codigosUnidades = new HashSet<>(codigos);
        return cacheViewsOrganizacaoService.listarTodasResponsabilidades().stream()
                .filter(leitura -> codigosUnidades.contains(leitura.unidadeCodigo()))
                .collect(HashMap::new,
                        (mapa, leitura) -> mapa.put(leitura.unidadeCodigo(), leitura.usuarioTitulo()),
                        HashMap::putAll);
    }

    private UnidadeHierarquiaService self() {
        return selfProvider.getObject();
    }

    private List<UnidadeDto> montarHierarquia(List<UnidadeHierarquiaLeitura> unidades, Map<Long, String> titulosResponsavel,
                                              @Nullable Predicate<UnidadeElegibilidadeInfo> elegibilidadeChecker) {
        Map<Long, UnidadeDto> mapaUnidades = new HashMap<>();
        Map<Long, List<UnidadeDto>> mapaFilhas = new HashMap<>();
        List<UnidadeDto> raizes = new ArrayList<>();

        for (UnidadeHierarquiaLeitura u : unidades) {
            var info = new UnidadeElegibilidadeInfo(u.codigo(), u.tipo(), titulosResponsavel.get(u.codigo()));
            boolean isElegivel = elegibilidadeChecker == null || elegibilidadeChecker.test(info);
            UnidadeDto dto = UnidadeDto.fromResumoObrigatorio(
                    u.codigo(),
                    u.nome(),
                    u.sigla(),
                    u.unidadeSuperiorCodigo(),
                    u.tipo().name(),
                    u.tituloTitular()
            );
            dto.setElegivel(isElegivel);

            Long codigo = u.codigo();
            mapaUnidades.put(codigo, dto);
            mapaFilhas.putIfAbsent(codigo, new ArrayList<>());
        }

        for (UnidadeHierarquiaLeitura u : unidades) {
            UnidadeDto dto = mapaUnidades.get(u.codigo());
            if (dto == null) {
                continue;
            }

            Optional.ofNullable(u.unidadeSuperiorCodigo()).ifPresentOrElse(
                    codigoPai -> mapaFilhas.computeIfAbsent(codigoPai, k -> new ArrayList<>()).add(dto),
                    () -> raizes.add(dto)
            );
        }

        return raizes.stream().map(raiz -> montarComSubunidades(raiz, mapaFilhas)).toList();
    }

    UnidadeDto montarComSubunidades(UnidadeDto dto, Map<Long, List<UnidadeDto>> mapaFilhas) {
        List<UnidadeDto> filhas = mapaFilhas.get(dto.getCodigo());
        if (filhas == null || filhas.isEmpty()) {
            dto.setSubunidades(new ArrayList<>());
            return dto;
        }

        List<UnidadeDto> subunidadesCompletas = filhas.stream()
                .map(filha -> montarComSubunidades(filha, mapaFilhas))
                .toList();

        dto.setSubunidades(subunidadesCompletas);

        return dto;
    }

    private UnidadeDto copiarComResponsavelAtual(UnidadeDto dto, Long codigo) {
        UnidadeDto copia = copiarArvore(dto);
        Unidade unidadeCompleta = unidadeService.buscarPorCodigo(codigo);
        UnidadeDto dtoCompleto = organizacaoDtoMapper.paraUnidadeDtoObrigatoria(unidadeCompleta);

        copia.setResponsavel(dtoCompleto.getResponsavel());
        copia.setTitular(dtoCompleto.getTitular());
        copia.setTipoResponsabilidade(dtoCompleto.getTipoResponsabilidade());
        copia.setDataInicioResponsabilidade(dtoCompleto.getDataInicioResponsabilidade());
        copia.setDataFimResponsabilidade(dtoCompleto.getDataFimResponsabilidade());

        return copia;
    }

    private UnidadeDto copiarArvore(UnidadeDto dto) {
        List<UnidadeDto> subunidades = dto.getSubunidades() == null
                ? new ArrayList<>()
                : dto.getSubunidades().stream()
                .map(this::copiarArvore)
                .toList();

        return UnidadeDto.builder()
                .codigo(dto.getCodigo())
                .nome(dto.getNome())
                .sigla(dto.getSigla())
                .codigoPai(dto.getCodigoPai())
                .tipo(dto.getTipo())
                .subunidades(subunidades)
                .tituloTitular(dto.getTituloTitular())
                .isElegivel(dto.isElegivel())
                .responsavel(dto.getResponsavel())
                .titular(dto.getTitular())
                .tipoResponsabilidade(dto.getTipoResponsabilidade())
                .dataInicioResponsabilidade(dto.getDataInicioResponsabilidade())
                .dataFimResponsabilidade(dto.getDataFimResponsabilidade())
                .build();
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


    private UnidadeDto toUnidadeDtoObrigatoria(Unidade unidade) {
        return organizacaoDtoMapper.paraUnidadeDtoObrigatoria(unidade);
    }
}
