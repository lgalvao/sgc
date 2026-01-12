package sgc.mapa.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.mapa.dto.MapaCompletoDto;
import sgc.mapa.dto.SalvarMapaRequest;
import sgc.mapa.mapper.MapaCompletoMapper;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.CompetenciaRepo;
import sgc.mapa.model.Mapa;
import sgc.mapa.model.MapaRepo;
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
 * @see CompetenciaService para operações de competências
 * @see AtividadeFacade para operações de atividades
 */
@Service
@Transactional
@Slf4j
public class MapaFacade {

    private final MapaRepo mapaRepo;
    private final CompetenciaRepo competenciaRepo;
    private final MapaCompletoMapper mapaCompletoMapper;
    private final MapaSalvamentoService mapaSalvamentoService;
    private final MapaVisualizacaoService mapaVisualizacaoService;
    private final ImpactoMapaService impactoMapaService;

    /**
     * Constructor with @Lazy injection to break circular dependency.
     * MapaFacade → MapaVisualizacaoService → SubprocessoFacade → SubprocessoCrudService → MapaFacade
     */
    public MapaFacade(
            MapaRepo mapaRepo,
            CompetenciaRepo competenciaRepo,
            MapaCompletoMapper mapaCompletoMapper,
            MapaSalvamentoService mapaSalvamentoService,
            MapaVisualizacaoService mapaVisualizacaoService,
            ImpactoMapaService impactoMapaService) {
        this.mapaRepo = mapaRepo;
        this.competenciaRepo = competenciaRepo;
        this.mapaCompletoMapper = mapaCompletoMapper;
        this.mapaSalvamentoService = mapaSalvamentoService;
        this.mapaVisualizacaoService = mapaVisualizacaoService;
        this.impactoMapaService = impactoMapaService;
    }

    // ===================================================================================
    // Operações de leitura
    // ===================================================================================

    @Transactional(readOnly = true)
    public List<Mapa> listar() {
        return mapaRepo.findAll();
    }

    @Transactional(readOnly = true)
    public Mapa obterPorCodigo(Long codigo) {
        return mapaRepo.findById(codigo)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Mapa", codigo));
    }

    @Transactional(readOnly = true)
    public Optional<Mapa> buscarMapaVigentePorUnidade(Long codigoUnidade) {
        return mapaRepo.findMapaVigenteByUnidade(codigoUnidade);
    }

    @Transactional(readOnly = true)
    public Optional<Mapa> buscarPorSubprocessoCodigo(Long codSubprocesso) {
        return mapaRepo.findBySubprocessoCodigo(codSubprocesso);
    }

    @Transactional(readOnly = true)
    public MapaCompletoDto obterMapaCompleto(Long codMapa, Long codSubprocesso) {
        Mapa mapa = mapaRepo.findById(codMapa)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Mapa não encontrado: %d".formatted(codMapa)));

        List<Competencia> competencias = competenciaRepo.findByMapaCodigo(codMapa);
        return mapaCompletoMapper.toDto(mapa, codSubprocesso, competencias);
    }

    // ===================================================================================
    // Operações de escrita básicas
    // ===================================================================================

    public Mapa salvar(Mapa mapa) {
        return mapaRepo.save(mapa);
    }

    public Mapa criar(Mapa mapa) {
        return mapaRepo.save(mapa);
    }

    public Mapa atualizar(Long codigo, Mapa mapa) {
        return mapaRepo.findById(codigo)
                .map(existente -> {
                    existente.setDataHoraDisponibilizado(mapa.getDataHoraDisponibilizado());
                    existente.setObservacoesDisponibilizacao(mapa.getObservacoesDisponibilizacao());
                    existente.setDataHoraHomologado(mapa.getDataHoraHomologado());
                    return mapaRepo.save(existente);
                })
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Mapa", codigo));
    }

    public void excluir(Long codigo) {
        if (!mapaRepo.existsById(codigo)) {
            throw new ErroEntidadeNaoEncontrada("Mapa", codigo);
        }
        mapaRepo.deleteById(codigo);
    }

    // ===================================================================================
    // Operação complexa delegada
    // ===================================================================================

    /**
     * Salva o mapa completo com competências e associações.
     *
     * @param codMapa                O código do mapa.
     * @param request                A requisição com os dados a salvar.
     * @return O DTO do mapa completo atualizado.
     */
    public MapaCompletoDto salvarMapaCompleto(
            Long codMapa, SalvarMapaRequest request) {
        return mapaSalvamentoService.salvarMapaCompleto(codMapa, request);
    }

    // ===================================================================================
    // Operações de visualização e análise
    // ===================================================================================

    /**
     * Obtém uma representação aninhada e formatada do mapa de um subprocesso.
     * Delega para {@link MapaVisualizacaoService}.
     *
     * @return Um {@link sgc.mapa.dto.visualizacao.MapaVisualizacaoDto} com a estrutura hierárquica completa do mapa.
     */
    @Transactional(readOnly = true)
    public sgc.mapa.dto.visualizacao.MapaVisualizacaoDto obterMapaParaVisualizacao(Subprocesso subprocesso) {
        return mapaVisualizacaoService.obterMapaParaVisualizacao(subprocesso);
    }

    /**
     * Analisa e retorna os impactos de uma revisão de mapa de competências.
     * Delega para {@link ImpactoMapaService}.
     *
     * <p>Compara o mapa em elaboração no subprocesso com o mapa vigente da unidade para identificar
     * atividades inseridas, removidas ou alteradas, e as competências afetadas.
     *
     * @param usuario O usuário autenticado (para verificação de permissões).
     * @return Um {@link sgc.mapa.dto.ImpactoMapaDto} com o detalhamento dos impactos.
     */
    @Transactional(readOnly = true)
    public sgc.mapa.dto.ImpactoMapaDto verificarImpactos(Subprocesso subprocesso, sgc.organizacao.model.Usuario usuario) {
        return impactoMapaService.verificarImpactos(subprocesso, usuario);
    }
}
