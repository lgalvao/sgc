package sgc.mapa.service;

import lombok.RequiredArgsConstructor;
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

import java.util.List;
import java.util.Optional;

/**
 * Serviço para operações básicas de CRUD em Mapas.
 *
 * <p>A lógica complexa de salvamento do mapa completo foi movida para
 * {@link MapaSalvamentoService}.
 */
@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class MapaService {

    private final MapaRepo mapaRepo;
    private final CompetenciaRepo competenciaRepo;
    private final MapaCompletoMapper mapaCompletoMapper;
    private final MapaSalvamentoService mapaSalvamentoService;

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
}
