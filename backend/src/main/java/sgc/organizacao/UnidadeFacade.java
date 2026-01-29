package sgc.organizacao;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;

import sgc.organizacao.dto.*;
import sgc.organizacao.mapper.UsuarioMapper;
import sgc.organizacao.model.*;
import sgc.organizacao.service.*;

import java.util.*;

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
    private final UnidadeRepositoryService unidadeRepositoryService;
    private final UnidadeMapaRepositoryService unidadeMapaRepositoryService;
    private final UsuarioRepositoryService usuarioRepositoryService;
    private final UsuarioMapper usuarioMapper;
    private final UnidadeHierarquiaService hierarquiaService;
    private final UnidadeMapaService mapaService;
    private final UnidadeResponsavelService responsavelService;

    public List<UnidadeDto> buscarArvoreHierarquica() {
        return hierarquiaService.buscarArvoreHierarquica();
    }

    public List<UnidadeDto> buscarTodasUnidades() {
        return hierarquiaService.buscarArvoreHierarquica();
    }

    public List<UnidadeDto> buscarArvoreComElegibilidade(
            boolean requerMapaVigente, java.util.Set<Long> unidadesBloqueadas) {
        Set<Long> unidadesComMapa = requerMapaVigente
                ? new HashSet<>(unidadeMapaRepositoryService.findAllUnidadeCodigos())
                : Collections.emptySet();

        return hierarquiaService.buscarArvoreComElegibilidade(u ->
                u.getTipo() != sgc.organizacao.model.TipoUnidade.INTERMEDIARIA
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
    public void definirMapaVigente(Long codigoUnidade, sgc.mapa.model.Mapa mapa) {
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
        return unidadeRepositoryService
                .findBySigla(sigla)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Unidade com sigla " + sigla + " não encontrada"));
    }

    public UnidadeDto buscarPorCodigo(Long codigo) {
        Unidade unidade = buscarEntidadePorId(codigo);
        return usuarioMapper.toUnidadeDto(unidade, false);
    }

    public Unidade buscarEntidadePorId(Long codigo) {
        Unidade unidade = unidadeRepositoryService.buscarPorId(codigo);
        if (unidade.getSituacao() != SituacaoUnidade.ATIVA) {
            throw new ErroEntidadeNaoEncontrada("Unidade", codigo);
        }
        return unidade;
    }

    public List<Unidade> buscarEntidadesPorIds(List<Long> codigos) {
        return unidadeRepositoryService.findAllById(codigos);
    }

    public List<Unidade> buscarTodasEntidadesComHierarquia() {
        return unidadeRepositoryService.findAllWithHierarquia();
    }

    public List<String> buscarSiglasPorIds(List<Long> codigos) {
        return unidadeRepositoryService.findSiglasByCodigos(codigos);
    }

    public List<UsuarioDto> buscarUsuariosPorUnidade(Long codigoUnidade) {
        return usuarioRepositoryService.findByUnidadeLotacaoCodigo(codigoUnidade).stream()
                .map(usuarioMapper::toUsuarioDto)
                .toList();
    }
}
