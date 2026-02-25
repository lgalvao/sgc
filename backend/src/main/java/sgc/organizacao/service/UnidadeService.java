package sgc.organizacao.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.model.ComumRepo;
import sgc.mapa.model.Mapa;
import sgc.organizacao.model.*;

import java.util.List;
import java.util.Map;

/**
 * Serviço consolidado para operações de Unidade.
 *
 * <p>Responsabilidades:
 * <ul>
 *   <li>Consultas básicas de unidades (por ID, sigla, lista)</li>
 *   <li>Gerenciamento de mapas vigentes de unidades</li>
 * </ul>
 *
 * <p>Este serviço consolida:
 * <ul>
 *   <li>UnidadeConsultaService (wrapper eliminado)</li>
 *   <li>UnidadeMapaService (lógica de mapas vigentes)</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UnidadeService {
    private final UnidadeRepo unidadeRepo;
    private final UnidadeMapaRepo unidadeMapaRepo;
    private final ComumRepo repo;

    public Unidade buscarPorId(Long codigo) {
        return repo.buscar(Unidade.class, Map.of("codigo", codigo, "situacao", SituacaoUnidade.ATIVA));
    }

    public Unidade buscarPorSigla(String sigla) {
        return repo.buscarPorSigla(Unidade.class, sigla);
    }

    public List<Unidade> porCodigos(List<Long> codigos) {
        return unidadeRepo.findAllById(codigos);
    }

    public List<Unidade> todasComHierarquia() {
        return unidadeRepo.findAllWithHierarquia();
    }

    public List<String> buscarSiglasPorIds(List<Long> codigos) {
        return unidadeRepo.findSiglasByCodigos(codigos);
    }

    public boolean verificarMapaVigente(Long codigoUnidade) {
        return unidadeMapaRepo.existsById(codigoUnidade);
    }

    public List<Long> buscarTodosCodigosUnidadesComMapa() {
        return unidadeMapaRepo.findAllUnidadeCodigos();
    }

    @Transactional
    public void definirMapaVigente(Long codigoUnidade, Mapa mapa) {
        UnidadeMapa unidadeMapa = unidadeMapaRepo.findById(codigoUnidade).orElse(new UnidadeMapa());
        unidadeMapa.setUnidadeCodigo(codigoUnidade);
        unidadeMapa.setMapaVigente(mapa);

        unidadeMapaRepo.save(unidadeMapa);
    }
}