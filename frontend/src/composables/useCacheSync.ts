import {useUnidadeStore} from "@/stores/unidade";
import {useOrganizacaoStore} from "@/stores/organizacao";
import {usePainelStore} from "@/stores/painel";
import {useProcessoStore} from "@/stores/processo";

const EVENTO_CACHE_ATUALIZADO = "org-cache-refreshed";

/**
 * Composable que mantém uma conexão SSE com o backend para receber notificações
 * de atualização dos caches organizacionais.
 *
 * Ao receber o evento "org-cache-refreshed", invalida os stores Pinia que
 * dependem de dados das views organizacionais, forçando recarga na próxima consulta.
 *
 * Deve ser chamado uma vez em App.vue após a autenticação do usuário.
 */
export function useCacheSync() {
    const unidadeStore = useUnidadeStore();
    const organizacaoStore = useOrganizacaoStore();
    const painelStore = usePainelStore();
    const processoStore = useProcessoStore();

    const source = new EventSource("/api/eventos");

    source.addEventListener(EVENTO_CACHE_ATUALIZADO, () => {
        unidadeStore.invalidarCache();
        organizacaoStore.$reset();
        painelStore.invalidar();
        processoStore.invalidar();
    });

    return () => {
        source.close();
    };
}
