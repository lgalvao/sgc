package sgc.organizacao.service;

import lombok.*;
import org.springframework.cache.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import sgc.comum.config.CacheConfig;
import sgc.comum.erros.*;
import sgc.mapa.model.Mapa;
import sgc.organizacao.dto.*;
import sgc.organizacao.model.*;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UnidadeService {
    private static final String SIGLA_ADMIN = "ADMIN";

    private final UnidadeRepo unidadeRepo;
    private final UnidadeMapaRepo unidadeMapaRepo;
    private final CacheOrganizacaoService cacheOrganizacaoService;

    public Unidade buscarPorCodigo(Long codigo) {
        return unidadeRepo.buscarPorCodigoComResponsavel(codigo)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada(Unidade.class.getSimpleName(), codigo));
    }

    @Cacheable(cacheNames = CacheConfig.CACHE_UNIDADE_POR_SIGLA, key = "#sigla.toUpperCase()", sync = true)
    public Unidade buscarPorSigla(String sigla) {
        return unidadeRepo.buscarPorSiglaComSuperior(sigla)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada(Unidade.class.getSimpleName(), sigla));
    }

    public Unidade buscarPorSiglaComResponsavel(String sigla) {
        return unidadeRepo.buscarPorSiglaComResponsavel(sigla)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada(Unidade.class.getSimpleName(), sigla));
    }

    @Cacheable(cacheNames = CacheConfig.CACHE_UNIDADE_CODIGO_POR_SIGLA, key = "#sigla.toUpperCase()", sync = true)
    public Long buscarCodigoPorSigla(String sigla) {
        return unidadeRepo.buscarCodigoAtivoPorSigla(sigla)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada(Unidade.class.getSimpleName(), sigla));
    }

    @Cacheable(cacheNames = CacheConfig.CACHE_UNIDADE_ADMIN, sync = true)
    public Unidade buscarAdmin() {
        return buscarPorSiglaComResponsavel(SIGLA_ADMIN);
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
        return buscarRegistroMapaVigenteComProcesso(codigoUnidade).isPresent();
    }

    public Optional<MapaVigenteReferenciaDto> buscarReferenciaMapaVigente(Long codigoUnidade) {
        return buscarRegistroMapaVigenteComProcesso(codigoUnidade)
                .map(UnidadeMapa::getMapaVigente)
                .map(Mapa::getSubprocesso)
                .map(subprocesso -> new MapaVigenteReferenciaDto(
                        subprocesso.getProcesso().getCodigo(),
                        subprocesso.getCodigo()
                ));
    }

    @Cacheable(cacheNames = CacheConfig.CACHE_UNIDADES_COM_MAPA, sync = true)
    public List<Long> buscarTodosCodigosUnidadesComMapa() {
        return unidadeMapaRepo.listarTodosCodigosUnidadeComMapaVigente();
    }

    public List<Long> buscarCodigosUnidadesSemMapaVigente() {
        return unidadeRepo.buscarCodigosUnidadesSemMapaVigente();
    }

    public List<UnidadeMapa> buscarMapasPorUnidades(List<Long> codigosUnidades) {
        if (codigosUnidades.isEmpty()) {
            return List.of();
        }
        return unidadeMapaRepo.listarMapasVigentesPorUnidades(codigosUnidades);
    }

    public Optional<Mapa> buscarMapaVigente(Long codigoUnidade) {
        return buscarRegistroMapaVigenteComProcesso(codigoUnidade)
                .map(UnidadeMapa::getMapaVigente);
    }

    @Transactional
    public void definirMapaVigente(Long codigoUnidade, Mapa mapa) {
        UnidadeMapa unidadeMapa = buscarRegistroMapaVigente(codigoUnidade).orElse(new UnidadeMapa());
        unidadeMapa.setUnidadeCodigo(codigoUnidade);
        unidadeMapa.setMapaVigente(mapa);

        unidadeMapaRepo.save(unidadeMapa);
        cacheOrganizacaoService.invalidarAposCommit();
    }

    @Transactional
    public void definirMapasVigentesEmBloco(Map<Long, Mapa> mapasPorUnidade) {
        if (mapasPorUnidade.isEmpty()) {
            return;
        }
        List<UnidadeMapa> existentes = unidadeMapaRepo.findAllById(mapasPorUnidade.keySet());
        Map<Long, UnidadeMapa> existentesPorCodigo = existentes.stream()
                .collect(java.util.stream.Collectors.toMap(UnidadeMapa::getUnidadeCodigoPersistido, um -> um));

        List<UnidadeMapa> paraAtualizar = mapasPorUnidade.entrySet().stream()
                .map(entry -> {
                    UnidadeMapa um = existentesPorCodigo.getOrDefault(entry.getKey(), new UnidadeMapa());
                    um.setUnidadeCodigo(entry.getKey());
                    um.setMapaVigente(entry.getValue());
                    return um;
                })
                .toList();

        unidadeMapaRepo.saveAll(paraAtualizar);
        cacheOrganizacaoService.invalidarAposCommit();
    }

    private Optional<UnidadeMapa> buscarRegistroMapaVigente(Long codigoUnidade) {
        return unidadeMapaRepo.findById(codigoUnidade);
    }

    private Optional<UnidadeMapa> buscarRegistroMapaVigenteComProcesso(Long codigoUnidade) {
        return unidadeMapaRepo.buscarMapaVigenteComProcesso(codigoUnidade);
    }
}
