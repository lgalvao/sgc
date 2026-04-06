package sgc.organizacao.service;

import lombok.*;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import sgc.comum.config.CacheConfig;
import sgc.comum.erros.*;
import sgc.organizacao.dto.*;
import sgc.mapa.model.*;
import sgc.organizacao.model.*;

import java.util.*;

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
    private static final String SIGLA_ADMIN = "ADMIN";

    private final UnidadeRepo unidadeRepo;
    private final UnidadeMapaRepo unidadeMapaRepo;
    public Unidade buscarPorCodigo(Long codigo) {
        return unidadeRepo.buscarPorCodigoComResponsavel(codigo)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada(Unidade.class.getSimpleName(), codigo));
    }

    public Unidade buscarPorSigla(String sigla) {
        return unidadeRepo.buscarPorSiglaComResponsavel(sigla)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada(Unidade.class.getSimpleName(), sigla));
    }

    @Cacheable(cacheNames = CacheConfig.CACHE_UNIDADE_ADMIN, sync = true)
    public Unidade buscarAdmin() {
        return buscarPorSigla(SIGLA_ADMIN);
    }

    public List<Unidade> buscarPorCodigos(List<Long> codigos) {
        return unidadeRepo.findAllById(codigos);
    }

    public List<String> buscarSiglasPorCodigos(List<Long> codigos) {
        return unidadeRepo.buscarSiglasPorCodigos(codigos);
    }

    public List<UnidadeResumoLeitura> buscarResumosPorCodigos(List<Long> codigos) {
        return unidadeRepo.listarResumosPorCodigos(codigos);
    }

    public String buscarSiglaPorCodigo(Long codigo) {
        return unidadeRepo.buscarSiglaPorCodigo(codigo)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada(Unidade.class.getSimpleName(), codigo));
    }

    public Unidade buscarPorCodigoComSuperior(Long codigo) {
        return unidadeRepo.buscarPorCodigoComSuperior(codigo)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada(Unidade.class.getSimpleName(), codigo));
    }

    public boolean temMapaVigente(Long codigoUnidade) {
        return unidadeMapaRepo.existsById(codigoUnidade);
    }

    public Optional<MapaVigenteReferenciaDto> buscarReferenciaMapaVigente(Long codigoUnidade) {
        return buscarRegistroMapaVigente(codigoUnidade)
                .map(UnidadeMapa::getMapaVigente)
                .map(Mapa::getSubprocesso)
                .map(subprocesso -> new MapaVigenteReferenciaDto(
                        subprocesso.getProcesso().getCodigo(),
                        subprocesso.getCodigo()
                ));
    }

    @Cacheable(cacheNames = CacheConfig.CACHE_UNIDADES_COM_MAPA, sync = true)
    public List<Long> buscarTodosCodigosUnidadesComMapa() {
        return unidadeMapaRepo.listarTodosCodigosUnidade();
    }

    public List<UnidadeMapa> buscarMapasPorUnidades(List<Long> codigosUnidades) {
        return unidadeMapaRepo.findAllById(codigosUnidades);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheConfig.CACHE_UNIDADES_COM_MAPA, allEntries = true),
            @CacheEvict(cacheNames = CacheConfig.CACHE_DIAGNOSTICO_ORGANIZACIONAL, allEntries = true)
    })
    public void definirMapaVigente(Long codigoUnidade, Mapa mapa) {
        UnidadeMapa unidadeMapa = buscarRegistroMapaVigente(codigoUnidade).orElse(new UnidadeMapa());
        unidadeMapa.setUnidadeCodigo(codigoUnidade);
        unidadeMapa.setMapaVigente(mapa);

        unidadeMapaRepo.save(unidadeMapa);
    }

    private Optional<UnidadeMapa> buscarRegistroMapaVigente(Long codigoUnidade) {
        return unidadeMapaRepo.findById(codigoUnidade);
    }
}
