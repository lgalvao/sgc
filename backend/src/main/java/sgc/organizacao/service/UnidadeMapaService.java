package sgc.organizacao.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.organizacao.model.UnidadeMapaRepo;

/**
 * Serviço especializado para gerenciar mapas vigentes de unidades.
 *
 * <p>Responsabilidades:
 * <ul>
 *   <li>Verificação de existência de mapa vigente</li>
 *   <li>Definição e atualização de mapa vigente</li>
 *   <li>Consultas relacionadas a mapas de unidades</li>
 * </ul>
 *
 * <p>Este serviço foi extraído de UnidadeFacade para respeitar o
 * Single Responsibility Principle (SRP).
 *
 */
@Service
@RequiredArgsConstructor
public class UnidadeMapaService {
    private final UnidadeMapaRepo unidadeMapaRepo;

    /**
     * Verifica se uma unidade possui mapa vigente.
     *
     * @param codigoUnidade código da unidade
     * @return true se a unidade possui mapa vigente
     */
    public boolean verificarMapaVigente(Long codigoUnidade) {
        return unidadeMapaRepo.existsById(codigoUnidade);
    }

    /**
     * Define ou atualiza o mapa vigente de uma unidade.
     *
     * @param codigoUnidade código da unidade
     * @param mapa          mapa a ser definido como vigente
     */
    @Transactional
    public void definirMapaVigente(Long codigoUnidade, sgc.mapa.model.Mapa mapa) {
        sgc.organizacao.model.UnidadeMapa unidadeMapa = unidadeMapaRepo.findById(codigoUnidade)
                .orElse(new sgc.organizacao.model.UnidadeMapa());
        unidadeMapa.setUnidadeCodigo(codigoUnidade);
        unidadeMapa.setMapaVigente(mapa);
        unidadeMapaRepo.save(unidadeMapa);
    }
}
