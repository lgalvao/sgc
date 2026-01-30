package sgc.organizacao;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.organizacao.dto.*;
import sgc.organizacao.mapper.UsuarioMapper;
import sgc.organizacao.model.*;
import sgc.organizacao.service.UnidadeConsultaService;
import sgc.organizacao.service.UnidadeHierarquiaService;
import sgc.organizacao.service.UnidadeMapaService;
import sgc.organizacao.service.UnidadeResponsavelService;
import sgc.organizacao.service.UsuarioConsultaService;

import java.util.*;
import java.util.Set;
import sgc.mapa.model.Mapa;
import sgc.organizacao.model.TipoUnidade;

/**
 * Facade para operações de unidades organizacionais.
 *
 * <p>Este facade delega operações para serviços especializados
 * <ul>
 *   <li>{@link UnidadeHierarquiaService} - Hierarquia e navegação</li>
 *   <li>{@link UnidadeMapaService} - Mapas vigentes</li>
 *   <li>{@link UnidadeResponsavelService} - Responsáveis e atribuições</li>
 * </ul>
 *
 * <p>A facade também mantém operações básicas de consulta e busca de entidades.
 */
@Service
@RequiredArgsConstructor
public class UnidadeFacade {
    private final UnidadeConsultaService unidadeConsultaService;
    private final UsuarioConsultaService usuarioConsultaService;
    private final UsuarioMapper usuarioMapper;
    private final UnidadeHierarquiaService hierarquiaService;
    private final UnidadeMapaService mapaService;
    private final UnidadeResponsavelService responsavelService;

    private static final String ENTIDADE_UNIDADE = "Unidade";

    public List<UnidadeDto> buscarArvoreHierarquica() {
        return hierarquiaService.buscarArvoreHierarquica();
    }

    public List<UnidadeDto> buscarTodasUnidades() {
        return hierarquiaService.buscarArvoreHierarquica();
    }

    public List<UnidadeDto> buscarArvoreComElegibilidade(
            boolean requerMapaVigente, Set<Long> unidadesBloqueadas) {
        Set<Long> unidadesComMapa = requerMapaVigente
                ? new HashSet<>(mapaService.buscarTodosCodigosUnidades())
                : Collections.emptySet();

        return hierarquiaService.buscarArvoreComElegibilidade(u ->
                u.getTipo() != TipoUnidade.INTERMEDIARIA
                        && (!requerMapaVigente || unidadesComMapa.contains(u.getCodigo()))
                        && !unidadesBloqueadas.contains(u.getCodigo())
        );
    }

    public List<Long> buscarIdsDescendentes(Long codigoUnidade) {
        return hierarquiaService.buscarIdsDescendentes(codigoUnidade);
    }

    public UnidadeDto buscarArvore(Long codigo) {
        return hierarquiaService.buscarArvore(codigo);
    }

    public List<String> buscarSiglasSubordinadas(String sigla) {
        return hierarquiaService.buscarSiglasSubordinadas(sigla);
    }

    public Optional<String> buscarSiglaSuperior(String sigla) {
        return hierarquiaService.buscarSiglaSuperior(sigla);
    }

    public List<UnidadeDto> buscarSubordinadas(Long codUnidade) {
        return hierarquiaService.buscarSubordinadas(codUnidade);
    }

    public boolean verificarMapaVigente(Long codigoUnidade) {
        return mapaService.verificarMapaVigente(codigoUnidade);
    }

    @Transactional
    public void definirMapaVigente(Long codigoUnidade, Mapa mapa) {
        mapaService.definirMapaVigente(codigoUnidade, mapa);
    }

    public List<AtribuicaoTemporariaDto> buscarTodasAtribuicoes() {
        return responsavelService.buscarTodasAtribuicoes();
    }

    public void criarAtribuicaoTemporaria(Long codUnidade, CriarAtribuicaoTemporariaRequest request) {
        responsavelService.criarAtribuicaoTemporaria(codUnidade, request);
    }

    @Transactional(readOnly = true)
    public Usuario buscarResponsavelAtual(String siglaUnidade) {
        return responsavelService.buscarResponsavelAtual(siglaUnidade);
    }

    public UnidadeResponsavelDto buscarResponsavelUnidade(Long unidadeCodigo) {
        return responsavelService.buscarResponsavelUnidade(unidadeCodigo);
    }

    @Transactional(readOnly = true)
    public Map<Long, UnidadeResponsavelDto> buscarResponsaveisUnidades(List<Long> unidadesCodigos) {
        return responsavelService.buscarResponsaveisUnidades(unidadesCodigos);
    }

    public UnidadeDto buscarPorSigla(String sigla) {
        Unidade unidade = buscarEntidadePorSigla(sigla);
        return usuarioMapper.toUnidadeDto(unidade, false);
    }

    public Unidade buscarEntidadePorSigla(String sigla) {
        return unidadeConsultaService.buscarPorSigla(sigla);
    }

    public UnidadeDto buscarPorCodigo(Long codigo) {
        Unidade unidade = buscarEntidadePorId(codigo);
        return usuarioMapper.toUnidadeDto(unidade, false);
    }

    public Unidade buscarEntidadePorId(Long codigo) {
        return unidadeConsultaService.buscarPorId(codigo);
    }

    public List<Unidade> buscarEntidadesPorIds(List<Long> codigos) {
        return unidadeConsultaService.buscarEntidadesPorIds(codigos);
    }

    public List<Unidade> buscarTodasEntidadesComHierarquia() {
        return unidadeConsultaService.buscarTodasEntidadesComHierarquia();
    }

    public List<String> buscarSiglasPorIds(List<Long> codigos) {
        return unidadeConsultaService.buscarSiglasPorIds(codigos);
    }

    public List<UsuarioDto> buscarUsuariosPorUnidade(Long codigoUnidade) {
        return usuarioConsultaService.buscarPorUnidadeLotacao(codigoUnidade).stream()
                .map(usuarioMapper::toUsuarioDto)
                .toList();
    }
}
