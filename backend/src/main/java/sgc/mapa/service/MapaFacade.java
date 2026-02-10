package sgc.mapa.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.mapa.dto.ImpactoMapaDto;
import sgc.mapa.dto.MapaCompletoDto;
import sgc.mapa.dto.SalvarMapaRequest;
import sgc.mapa.dto.visualizacao.MapaVisualizacaoDto;
import sgc.mapa.mapper.MapaCompletoMapper;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.Mapa;
import sgc.organizacao.model.Usuario;
import sgc.subprocesso.model.Subprocesso;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class MapaFacade {
    private final MapaCompletoMapper mapaCompletoMapper;
    private final MapaSalvamentoService mapaSalvamentoService;
    private final MapaManutencaoService mapaManutencaoService;
    private final MapaVisualizacaoService mapaVisualizacaoService;
    private final ImpactoMapaService impactoMapaService;

    @Transactional(readOnly = true)
    public List<Mapa> listar() {
        return mapaManutencaoService.listarTodosMapas();
    }

    @Transactional(readOnly = true)
    public Mapa obterPorCodigo(Long codigo) {
        return mapaManutencaoService.buscarMapaPorCodigo(codigo);
    }

    @Transactional(readOnly = true)
    public Optional<Mapa> buscarMapaVigentePorUnidade(Long codigoUnidade) {
        return mapaManutencaoService.buscarMapaVigentePorUnidade(codigoUnidade);
    }

    @Transactional(readOnly = true)
    public Optional<Mapa> buscarPorSubprocessoCodigo(Long codSubprocesso) {
        return mapaManutencaoService.buscarMapaPorSubprocessoCodigo(codSubprocesso);
    }

    @Transactional(readOnly = true)
    public MapaCompletoDto obterMapaCompleto(Long codMapa, Long codSubprocesso) {
        Mapa mapa = mapaManutencaoService.buscarMapaPorCodigo(codMapa);
        List<Competencia> competencias = mapaManutencaoService.buscarCompetenciasPorCodMapa(codMapa);
        return mapaCompletoMapper.toDto(mapa, codSubprocesso, competencias);
    }

    public Mapa salvar(Mapa mapa) {
        return mapaManutencaoService.salvarMapa(mapa);
    }

    public Mapa atualizar(Long codigo, Mapa mapa) {
        Mapa existente = mapaManutencaoService.buscarMapaPorCodigo(codigo);
        existente.setDataHoraDisponibilizado(mapa.getDataHoraDisponibilizado());
        existente.setObservacoesDisponibilizacao(mapa.getObservacoesDisponibilizacao());
        existente.setDataHoraHomologado(mapa.getDataHoraHomologado());
        return mapaManutencaoService.salvarMapa(existente);
    }

    public void excluir(Long codigo) {
        mapaManutencaoService.excluirMapa(codigo);
    }

    public MapaCompletoDto salvarMapaCompleto(Long codMapa, SalvarMapaRequest request) {
        return mapaSalvamentoService.salvarMapaCompleto(codMapa, request);
    }

    @Transactional(readOnly = true)
    public MapaVisualizacaoDto obterMapaParaVisualizacao(Subprocesso subprocesso) {
        return mapaVisualizacaoService.obterMapaParaVisualizacao(subprocesso);
    }

    @Transactional(readOnly = true)
    public ImpactoMapaDto verificarImpactos(Subprocesso subprocesso, Usuario usuario) {
        return impactoMapaService.verificarImpactos(subprocesso, usuario);
    }
}