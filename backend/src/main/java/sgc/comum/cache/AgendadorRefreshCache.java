package sgc.comum.cache;

import lombok.*;
import lombok.extern.slf4j.*;
import org.springframework.cache.*;
import org.springframework.scheduling.annotation.*;
import org.springframework.stereotype.*;
import sgc.comum.config.CacheConfig;
import sgc.organizacao.service.*;

/**
 * Agendador responsável por invalidar e recarregar periodicamente os caches das views.
 *
 * <p>Executa a cada 10 minutos, evitando miss frios e limitando a staleness máxima
 * independentemente do TTL configurado. Após a recarga, transmite um evento SSE
 * para que os clientes Vue atualizem seus stores.
 *
 * <p>A ordem de recarga segue a dependência entre as views:
 * VW_UNIDADE → VW_RESPONSABILIDADE → VW_USUARIO_PERFIL_UNIDADE (por último).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AgendadorRefreshCache {

    private final CacheViewsOrganizacaoService cacheViewsOrganizacaoService;
    private final UnidadeHierarquiaService unidadeHierarquiaService;
    private final CacheManager cacheManager;
    private final RegistroSseEmitter registroSseEmitter;

    private static final long INTERVALO_REFRESH_MS = 600_000;

    @Scheduled(fixedDelay = INTERVALO_REFRESH_MS)
    public void atualizarTudo() {
        try {
            log.debug("Iniciando refresh periódico dos caches organizacionais...");
            evictarTodosCaches();
            recarregarCaches();
            registroSseEmitter.transmitir("org-cache-refreshed");
            log.debug("Refresh periódico dos caches concluído.");
        } catch (Exception e) {
            log.warn("Falha no refresh periódico dos caches", e);
        }
    }

    public void evictarTodosCaches() {
        cacheViewsOrganizacaoService.evictarUnidades();
        cacheViewsOrganizacaoService.evictarUsuarios();
        cacheViewsOrganizacaoService.evictarResponsabilidades();
        cacheViewsOrganizacaoService.evictarPerfisUnidade();
        limparCacheDerivado(CacheConfig.CACHE_ARVORE_UNIDADES);
        limparCacheDerivado(CacheConfig.CACHE_MAPA_HIERARQUIA_UNIDADES);
        limparCacheDerivado(CacheConfig.CACHE_MAPA_FILHO_PAI);
        limparCacheDerivado(CacheConfig.CACHE_UNIDADE_ADMIN);
        limparCacheDerivado(CacheConfig.CACHE_UNIDADE_POR_SIGLA);
        limparCacheDerivado(CacheConfig.CACHE_UNIDADE_CODIGO_POR_SIGLA);
        limparCacheDerivado(CacheConfig.CACHE_UNIDADES_COM_MAPA);
        limparCacheDerivado(CacheConfig.CACHE_USUARIO_PERFIS);
        limparCacheDerivado(CacheConfig.CACHE_USUARIO_AUTORIZACOES);
        limparCacheDerivado(CacheConfig.CACHE_DIAGNOSTICO_ORGANIZACIONAL);
    }

    public void recarregarCaches() {
        cacheViewsOrganizacaoService.listarTodasUnidades();
        cacheViewsOrganizacaoService.listarTodosUsuarios();
        cacheViewsOrganizacaoService.listarTodasResponsabilidades();
        cacheViewsOrganizacaoService.listarTodosPerfisUnidade();
        unidadeHierarquiaService.buscarArvoreHierarquica();
        unidadeHierarquiaService.buscarMapaHierarquia();
        unidadeHierarquiaService.buscarMapaFilhoPai();
    }

    private void limparCacheDerivado(String nome) {
        Cache cache = cacheManager.getCache(nome);
        if (cache != null) {
            cache.clear();
        }
    }
}
