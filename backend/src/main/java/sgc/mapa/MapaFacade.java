package sgc.mapa;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.mapa.dto.ImpactoMapaResponse;
import sgc.mapa.dto.MapaVisualizacaoResponse;
import sgc.mapa.dto.SalvarMapaRequest;
import sgc.mapa.model.Mapa;
import sgc.mapa.service.ImpactoMapaService;
import sgc.mapa.service.MapaManutencaoService;
import sgc.mapa.service.MapaSalvamentoService;
import sgc.mapa.service.MapaVisualizacaoService;
import sgc.organizacao.model.Usuario;
import sgc.subprocesso.model.Subprocesso;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MapaFacade {
    private final MapaSalvamentoService mapaSalvamentoService;
    private final MapaManutencaoService mapaManutencaoService;
    private final MapaVisualizacaoService mapaVisualizacaoService;
    private final ImpactoMapaService impactoMapaService;

    public List<Mapa> todosMapas() {
        return mapaManutencaoService.listarTodosMapas();
    }

    public Mapa mapaPorCodigo(Long codigo) {
        return mapaManutencaoService.buscarMapaPorCodigo(codigo);
    }

    public Optional<Mapa> mapaPorSubprocesso(Long codSubprocesso) {
        return mapaManutencaoService.buscarMapaPorSubprocessoCodigo(codSubprocesso);
    }
    public Mapa mapaCompletoPorSubprocesso(Long codSubprocesso) {
        return mapaManutencaoService.buscarMapaCompletoPorSubprocesso(codSubprocesso);
    }

    public Optional<Mapa> mapaVigentePorUnidade(Long codigoUnidade) {
        return mapaManutencaoService.buscarMapaVigentePorUnidade(codigoUnidade);
    }

    public MapaVisualizacaoResponse mapaParaVisualizacao(Subprocesso subprocesso) {
        return mapaVisualizacaoService.obterMapaParaVisualizacao(subprocesso);
    }

    public ImpactoMapaResponse verificarImpactos(Subprocesso subprocesso, Usuario usuario) {
        return impactoMapaService.verificarImpactos(subprocesso, usuario);
    }

    @Transactional
    public Mapa salvar(Mapa mapa) {
        return mapaManutencaoService.salvarMapa(mapa);
    }

    @Transactional
    public void excluir(Long codigo) {
        mapaManutencaoService.excluirMapa(codigo);
    }

    @Transactional
    public Mapa atualizar(Long codigo, Mapa mapa) {
        Mapa existente = mapaManutencaoService.buscarMapaPorCodigo(codigo)
                .setDataHoraDisponibilizado(mapa.getDataHoraDisponibilizado())
                .setObservacoesDisponibilizacao(mapa.getObservacoesDisponibilizacao())
                .setDataHoraHomologado(mapa.getDataHoraHomologado());

        return mapaManutencaoService.salvarMapa(existente);
    }

    @Transactional
    public Mapa salvarMapaCompleto(Long codMapa, SalvarMapaRequest request) {
        return mapaSalvamentoService.salvarMapaCompleto(codMapa, request);
    }
}