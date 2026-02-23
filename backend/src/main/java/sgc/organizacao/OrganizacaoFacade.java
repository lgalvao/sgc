package sgc.organizacao;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.mapa.model.Mapa;
import sgc.organizacao.dto.AtribuicaoDto;
import sgc.organizacao.dto.CriarAtribuicaoRequest;
import sgc.organizacao.dto.UnidadeDto;
import sgc.organizacao.dto.UnidadeResponsavelDto;
import sgc.organizacao.model.TipoUnidade;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.organizacao.service.ResponsavelUnidadeService;
import sgc.organizacao.service.UnidadeHierarquiaService;
import sgc.organizacao.service.UnidadeService;
import sgc.organizacao.service.UsuarioService;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrganizacaoFacade {
    private final UsuarioFacade usuarioFacade;
    private final UnidadeService unidadeService;
    private final UsuarioService usuarioService;
    private final UnidadeHierarquiaService hierarquiaService;
    private final ResponsavelUnidadeService responsavelService;

    public Usuario obterUsuarioAutenticado() {
        return usuarioFacade.usuarioAutenticado();
    }

    public @Nullable String extrairTituloUsuario(@Nullable Object principal) {
        return usuarioFacade.extrairTituloUsuario(principal);
    }

    public Usuario buscarPorLogin(String login) {
        return usuarioFacade.buscarPorLogin(login);
    }

    public List<UnidadeDto> buscarArvoreHierarquica() {
        return hierarquiaService.buscarArvoreHierarquica();
    }

    public List<UnidadeDto> buscarTodasUnidades() {
        return hierarquiaService.buscarArvoreHierarquica();
    }

    public List<UnidadeDto> buscarArvoreComElegibilidade(
            boolean requerMapaVigente, Set<Long> unidadesBloqueadas) {

        Set<Long> unidadesComMapa = requerMapaVigente
                ? new HashSet<>(unidadeService.buscarTodosCodigosUnidadesComMapa())
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

    public List<Long> buscarIdsDescendentes(Long codigoUnidade, Map<Long, List<Long>> mapPaiFilhos) {
        return hierarquiaService.buscarDescendentes(codigoUnidade, mapPaiFilhos);
    }

    public Map<Long, List<Long>> buscarMapaHierarquia() {
        return hierarquiaService.buscarMapaHierarquia();
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
        return unidadeService.verificarMapaVigente(codigoUnidade);
    }

    public List<AtribuicaoDto> buscarTodasAtribuicoes() {
        return responsavelService.buscarTodasAtribuicoes();
    }

    @Transactional
    public void definirMapaVigente(Long codigoUnidade, Mapa mapa) {
        unidadeService.definirMapaVigente(codigoUnidade, mapa);
    }

    @Transactional
    public void criarAtribuicaoTemporaria(Long codUnidade, CriarAtribuicaoRequest request) {
        responsavelService.criarAtribuicaoTemporaria(codUnidade, request);
    }

    @Transactional(readOnly = true)
    public Usuario buscarResponsavelAtual(String siglaUnidade) {
        return responsavelService.buscarResponsavelAtual(siglaUnidade);
    }

    public UnidadeResponsavelDto buscarResponsavelUnidade(Long unidadeCodigo) {
        return responsavelService.buscarResponsavelUnidade(unidadeCodigo);
    }

    public Map<Long, UnidadeResponsavelDto> buscarResponsaveisUnidades(List<Long> unidadesCodigos) {
        return responsavelService.buscarResponsaveisUnidades(unidadesCodigos);
    }

    public UnidadeDto buscarPorSigla(String sigla) {
        Unidade unidade = buscarEntidadePorSigla(sigla);
        return UnidadeDto.fromEntity(unidade);
    }

    public Unidade buscarEntidadePorSigla(String sigla) {
        return unidadeService.buscarPorSigla(sigla);
    }

    public UnidadeDto dtoPorCodigo(Long codigo) {
        Unidade unidade = unidadePorCodigo(codigo);
        return UnidadeDto.fromEntity(unidade);
    }

    public Unidade unidadePorCodigo(Long codigo) {
        return unidadeService.buscarPorId(codigo);
    }

    public List<Unidade> unidadesPorCodigos(List<Long> codigos) {
        return unidadeService.porCodigos(codigos);
    }

    public List<Unidade> unidadesComHierarquia() {
        return unidadeService.todasComHierarquia();
    }

    public List<String> siglasUnidadesPorCodigos(List<Long> codigos) {
        return unidadeService.buscarSiglasPorIds(codigos);
    }

    public List<Usuario> usuariosPorCodigoUnidade(Long codigoUnidade) {
        return usuarioService.buscarPorUnidadeLotacao(codigoUnidade);
    }
}
