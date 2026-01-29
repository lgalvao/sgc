package sgc.mapa.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.mapa.dto.MapaCompletoDto;
import sgc.mapa.dto.SalvarMapaRequest;
import sgc.mapa.mapper.MapaCompletoMapper;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.Mapa;
import sgc.subprocesso.model.Subprocesso;

import java.util.List;
import java.util.Optional;

/**
 * Facade para operações com Mapas de Competências.
 *
 * <p>Esta classe implementa o padrão Facade para simplificar a interface
 * de uso e centralizar a coordenação entre múltiplos serviços relacionados a mapas.
 *
 * <p><b>Arquitetura:</b> Controllers devem usar APENAS esta Facade, nunca serviços especializados diretamente.
 *
 * @see MapaSalvamentoService para lógica complexa de salvamento
 * @see MapaVisualizacaoService para operações de visualização
 * @see AtividadeFacade para operações de atividades
 */
@Service
@Transactional
@Slf4j
public class MapaFacade {

    private final MapaRepositoryService mapaService;
    private final CompetenciaRepositoryService competenciaService;
    private final MapaCompletoMapper mapaCompletoMapper;
    private final MapaSalvamentoService mapaSalvamentoService;
    private final MapaVisualizacaoService mapaVisualizacaoService;
    private final ImpactoMapaService impactoMapaService;
    private final sgc.comum.repo.RepositorioComum repo;

    public MapaFacade(
            MapaRepositoryService mapaService,
            CompetenciaRepositoryService competenciaService,
            MapaCompletoMapper mapaCompletoMapper,
            MapaSalvamentoService mapaSalvamentoService,
            MapaVisualizacaoService mapaVisualizacaoService,
            ImpactoMapaService impactoMapaService,
            sgc.comum.repo.RepositorioComum repo) {
        this.mapaService = mapaService;
        this.competenciaService = competenciaService;
        this.mapaCompletoMapper = mapaCompletoMapper;
        this.mapaSalvamentoService = mapaSalvamentoService;
        this.mapaVisualizacaoService = mapaVisualizacaoService;
        this.impactoMapaService = impactoMapaService;
        this.repo = repo;
    }

    // ===================================================================================
    // Operações de leitura
    // ===================================================================================

    @Transactional(readOnly = true)
    public List<Mapa> listar() {
        return mapaService.findAll();
    }

    @Transactional(readOnly = true)
    public Mapa obterPorCodigo(Long codigo) {
        return repo.buscar(Mapa.class, codigo);
    }

    @Transactional(readOnly = true)
    public Optional<Mapa> buscarMapaVigentePorUnidade(Long codigoUnidade) {
        return mapaService.findMapaVigenteByUnidade(codigoUnidade);
    }

    @Transactional(readOnly = true)
    public Optional<Mapa> buscarPorSubprocessoCodigo(Long codSubprocesso) {
        return mapaService.findBySubprocessoCodigo(codSubprocesso);
    }

    @Transactional(readOnly = true)
    public MapaCompletoDto obterMapaCompleto(Long codMapa, Long codSubprocesso) {
        Mapa mapa = repo.buscar(Mapa.class, codMapa);

        List<Competencia> competencias = competenciaService.findByMapaCodigo(codMapa);
        return mapaCompletoMapper.toDto(mapa, codSubprocesso, competencias);
    }

    // ===================================================================================
    // Operações de escrita básicas
    // ===================================================================================

    public Mapa salvar(Mapa mapa) {
        return mapaService.salvar(mapa);
    }

    public Mapa criar(Mapa mapa) {
        return mapaService.salvar(mapa);
    }

    public Mapa atualizar(Long codigo, Mapa mapa) {
        Mapa existente = repo.buscar(Mapa.class, codigo);
        existente.setDataHoraDisponibilizado(mapa.getDataHoraDisponibilizado());
        existente.setObservacoesDisponibilizacao(mapa.getObservacoesDisponibilizacao());
        existente.setDataHoraHomologado(mapa.getDataHoraHomologado());
        return mapaService.salvar(existente);
    }

    public void excluir(Long codigo) {
        if (!mapaService.existsById(codigo)) {
            throw new ErroEntidadeNaoEncontrada("Mapa", codigo);
        }
        mapaService.deleteById(codigo);
    }

    // ===================================================================================
    // Operação complexa delegada
    // ===================================================================================

    public MapaCompletoDto salvarMapaCompleto(Long codMapa, SalvarMapaRequest request) {
        return mapaSalvamentoService.salvarMapaCompleto(codMapa, request);
    }

    // ===================================================================================
    // Operações de visualização e análise
    // ===================================================================================

    @Transactional(readOnly = true)
    public sgc.mapa.dto.visualizacao.MapaVisualizacaoDto obterMapaParaVisualizacao(Subprocesso subprocesso) {
        return mapaVisualizacaoService.obterMapaParaVisualizacao(subprocesso);
    }

    @Transactional(readOnly = true)
    public sgc.mapa.dto.ImpactoMapaDto verificarImpactos(Subprocesso subprocesso, sgc.organizacao.model.Usuario usuario) {
        return impactoMapaService.verificarImpactos(subprocesso, usuario);
    }
}