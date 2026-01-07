package sgc.organizacao;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.organizacao.dto.AtribuicaoTemporariaDto;
import sgc.organizacao.dto.CriarAtribuicaoTemporariaReq;
import sgc.organizacao.dto.UnidadeDto;
import sgc.organizacao.dto.UsuarioDto;
import sgc.organizacao.mapper.UsuarioMapper;
import sgc.organizacao.model.*;
import sgc.processo.model.TipoProcesso;
import sgc.processo.service.ProcessoConsultaService;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class UnidadeService {
    private final UnidadeRepo unidadeRepo;
    private final sgc.organizacao.model.UnidadeMapaRepo unidadeMapaRepo;
    private final sgc.mapa.service.MapaService mapaService;
    private final UsuarioService usuarioService;
    private final AtribuicaoTemporariaRepo atribuicaoTemporariaRepo;
    private final ProcessoConsultaService processoConsultaService;
    private final UsuarioMapper usuarioMapper;

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
            TipoProcesso tipoProcesso, Long codProcesso) {
        boolean requerMapaVigente =
                tipoProcesso == TipoProcesso.REVISAO || tipoProcesso == TipoProcesso.DIAGNOSTICO;

        List<Unidade> todasUnidades = unidadeRepo.findAllWithHierarquia();

        return montarHierarquiaComElegibilidade(todasUnidades, requerMapaVigente, codProcesso);
    }

    private List<UnidadeDto> montarHierarquiaComElegibilidade(
            List<Unidade> unidades, boolean requerMapaVigente, Long codProcessoIgnorar) {
        Map<Long, UnidadeDto> mapaUnidades = new HashMap<>();
        Map<Long, List<UnidadeDto>> mapaFilhas = new HashMap<>();
        List<UnidadeDto> raizes = new ArrayList<>();

        Set<Long> unidadesEmProcessoAtivo = getUnidadesEmProcessosAtivos(codProcessoIgnorar);

        // Otimização: Busca em lote os IDs das unidades que possuem mapa para evitar N+1
        Set<Long> unidadesComMapa = requerMapaVigente
                ? new HashSet<>(unidadeMapaRepo.findAllUnidadeCodigos())
                : Collections.emptySet();

        for (Unidade u : unidades) {
            // Elegibilidade simples (não recursiva):
            // 3. NÃO está em outro processo ativo
            boolean isElegivel =
                    u.getTipo() != sgc.organizacao.model.TipoUnidade.INTERMEDIARIA
                            && (!requerMapaVigente || unidadesComMapa.contains(u.getCodigo()))
                            && !unidadesEmProcessoAtivo.contains(u.getCodigo());

            UnidadeDto dto = usuarioMapper.toUnidadeDto(u, isElegivel);

            mapaUnidades.put(u.getCodigo(), dto);
            mapaFilhas.putIfAbsent(u.getCodigo(), new ArrayList<>());
        }

        for (Unidade u : unidades) {
            UnidadeDto dto = mapaUnidades.get(u.getCodigo());

            if (u.getUnidadeSuperior() == null) {
                raizes.add(dto);
            } else {
                Long codigoPai = u.getUnidadeSuperior().getCodigo();
                mapaFilhas.computeIfAbsent(codigoPai, k -> new ArrayList<>()).add(dto);
            }
        }

        List<UnidadeDto> resultado = new ArrayList<>();
        for (UnidadeDto raiz : raizes) {
            resultado.add(montarComSubunidades(raiz, mapaFilhas));
        }

        return resultado;
    }

    private Set<Long> getUnidadesEmProcessosAtivos(Long codProcessoIgnorar) {
        return processoConsultaService.buscarIdsUnidadesEmProcessosAtivos(codProcessoIgnorar);
    }

    public void criarAtribuicaoTemporaria(
            Long codUnidade, CriarAtribuicaoTemporariaReq request) {
        Unidade unidade =
                unidadeRepo
                        .findById(codUnidade)
                        .orElseThrow(
                                () ->
                                        new ErroEntidadeNaoEncontrada(
                                                "Unidade com codigo "
                                                        + codUnidade
                                                        + " não encontrada"));

        Usuario usuario = usuarioService.buscarEntidadePorId(request.tituloEleitoralUsuario());

        // Valida se o usuário pertence à unidade (simplificado, poderia ser mais rigoroso)
        // Se a unidade não for a mesma e nem superior/subordinada, talvez devesse bloquear?
        // O requisito CDU-28 diz apenas "ADMIN seleciona servidor" da lista de servidores da unidade.

        // Validação de datas
        java.time.LocalDate inicio = request.dataInicio() != null ? request.dataInicio() : java.time.LocalDate.now();
        if (request.dataTermino().isBefore(inicio)) {
            throw new sgc.comum.erros.ErroValidacao("A data de término deve ser posterior à data de início.");
        }

        AtribuicaoTemporaria atribuicao = new AtribuicaoTemporaria();
        atribuicao.setUnidade(unidade);
        atribuicao.setUsuarioTitulo(usuario.getTituloEleitoral());
        atribuicao.setUsuarioMatricula(usuario.getMatricula());
        atribuicao.setDataInicio(request.dataInicio() != null ? request.dataInicio().atStartOfDay() : LocalDateTime.now());
        atribuicao.setDataTermino(request.dataTermino().atTime(23, 59, 59));
        atribuicao.setJustificativa(request.justificativa());

        atribuicaoTemporariaRepo.save(atribuicao);
    }

    public boolean verificarMapaVigente(Long codigoUnidade) {
        return mapaService.buscarMapaVigentePorUnidade(codigoUnidade).isPresent();
    }

    public List<UsuarioDto> buscarUsuariosPorUnidade(Long codigoUnidade) {
        return usuarioService.buscarUsuariosPorUnidade(codigoUnidade);
    }

    private List<UnidadeDto> montarHierarquia(List<Unidade> unidades) {
        Map<Long, UnidadeDto> mapaUnidades = new HashMap<>();
        Map<Long, List<UnidadeDto>> mapaFilhas = new HashMap<>();
        List<UnidadeDto> raizes = new ArrayList<>();

        for (Unidade u : unidades) {
            UnidadeDto dto = usuarioMapper.toUnidadeDto(u);

            mapaUnidades.put(u.getCodigo(), dto);
            mapaFilhas.putIfAbsent(u.getCodigo(), new ArrayList<>());
        }

        for (Unidade u : unidades) {
            UnidadeDto dto = mapaUnidades.get(u.getCodigo());

            if (u.getUnidadeSuperior() == null) {
                raizes.add(dto);
            } else {
                Long codigoPai = u.getUnidadeSuperior().getCodigo();
                mapaFilhas.computeIfAbsent(codigoPai, k -> new ArrayList<>()).add(dto);
            }
        }

        List<UnidadeDto> resultado = new ArrayList<>();
        for (UnidadeDto raiz : raizes) {
            resultado.add(montarComSubunidades(raiz, mapaFilhas));
        }

        return resultado;
    }

    private UnidadeDto montarComSubunidades(
            UnidadeDto dto, Map<Long, List<UnidadeDto>> mapaFilhas) {
        List<UnidadeDto> filhas = mapaFilhas.get(dto.getCodigo());
        if (filhas == null || filhas.isEmpty()) {
            return dto;
        }

        List<UnidadeDto> subunidadesCompletas = new ArrayList<>();
        for (UnidadeDto filha : filhas) {
            subunidadesCompletas.add(montarComSubunidades(filha, mapaFilhas));
        }

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
                .orElseThrow(
                        () ->
                                new ErroEntidadeNaoEncontrada(
                                        "Unidade com sigla " + sigla + " não encontrada"));
    }

    public UnidadeDto buscarPorCodigo(Long codigo) {
        Unidade unidade = buscarEntidadePorId(codigo);
        return usuarioMapper.toUnidadeDto(unidade, false);
    }

    public Unidade buscarEntidadePorId(Long codigo) {
        return unidadeRepo
                .findById(codigo)
                .orElseThrow(
                        () ->
                                new ErroEntidadeNaoEncontrada(
                                        "Unidade com codigo "
                                                + codigo
                                                + " não encontrada"));
    }

    public List<Unidade> listarSubordinadas(Long codigoPai) {
        return unidadeRepo.findByUnidadeSuperiorCodigo(codigoPai);
    }

    /**
     * Busca IDs de todas as unidades subordinadas (recursivamente) em memória,
     * utilizando a lista cacheada de unidades para evitar N+1 queries.
     * Resultado é cacheado para evitar reconstrução do mapa O(N) em chamadas repetidas.
     */
    @Cacheable(value = "unidadeDescendentes", key = "#codigoUnidade")
    public List<Long> buscarIdsDescendentes(Long codigoUnidade) {
        List<Unidade> todas = buscarTodasEntidadesComHierarquia();

        // Mapa para lookup rápido: Pai -> Lista de Filhos
        Map<Long, List<Long>> mapPaiFilhos = new HashMap<>();
        for (Unidade u : todas) {
            if (u.getUnidadeSuperior() != null) {
                mapPaiFilhos.computeIfAbsent(u.getUnidadeSuperior().getCodigo(), k -> new ArrayList<>())
                        .add(u.getCodigo());
            }
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

    /**
     * Busca todas as unidades com hierarquia.
     * Resultado é cacheado pois a estrutura organizacional muda raramente.
     */
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
        return buscarNaHierarquia(todas, codigo);
    }

    public List<String> buscarSiglasSubordinadas(String sigla) {
        List<UnidadeDto> todas = buscarTodasUnidades();
        UnidadeDto raiz = buscarNaHierarquiaPorSigla(todas, sigla);

        if (raiz == null) {
            throw new ErroEntidadeNaoEncontrada(
                    "Unidade com sigla " + sigla + " não encontrada na hierarquia");
        }

        List<String> resultado = new ArrayList<>();
        coletarSiglas(raiz, resultado);
        return resultado;
    }

    public String buscarSiglaSuperior(String sigla) {
        Unidade unidade =
                unidadeRepo
                        .findBySigla(sigla)
                        .orElseThrow(
                                () ->
                                        new ErroEntidadeNaoEncontrada(
                                                "Unidade com sigla " + sigla + " não encontrada"));

        if (unidade.getUnidadeSuperior() != null) {
            return unidade.getUnidadeSuperior().getSigla();
        }
        return null;
    }

    private UnidadeDto buscarNaHierarquia(List<UnidadeDto> lista, Long codigo) {
        for (UnidadeDto u : lista) {
            if (u.getCodigo().equals(codigo)) return u;
            if (u.getSubunidades() != null) {
                UnidadeDto found = buscarNaHierarquia(u.getSubunidades(), codigo);
                if (found != null) return found;
            }
        }
        return null;
    }

    private UnidadeDto buscarNaHierarquiaPorSigla(List<UnidadeDto> lista, String sigla) {
        for (UnidadeDto u : lista) {
            if (u.getSigla().equals(sigla)) return u;
            if (u.getSubunidades() != null) {
                UnidadeDto found = buscarNaHierarquiaPorSigla(u.getSubunidades(), sigla);
                if (found != null) return found;
            }
        }
        return null;
    }

    private void coletarSiglas(UnidadeDto unidade, List<String> resultado) {
        resultado.add(unidade.getSigla());
        if (unidade.getSubunidades() != null) {
            for (UnidadeDto filha : unidade.getSubunidades()) {
                coletarSiglas(filha, resultado);
            }
        }
    }
}
