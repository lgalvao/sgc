import {useUnidadeStore} from "@/stores/unidade";
import {useOrganizacaoStore} from "@/stores/organizacao";
import {usePainelStore} from "@/stores/painel";
import {logger} from "@/utils";

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

    const source = new EventSource("/api/eventos");
    let encerradoManualmente = false;

    source.addEventListener(EVENTO_CACHE_ATUALIZADO, () => {
        unidadeStore.invalidar();
        organizacaoStore.invalidar();
        painelStore.invalidar();
    });

    source.addEventListener("error", (event) => {
        if (encerradoManualmente) {
            return;
        }

        if (source.readyState !== EventSource.OPEN) {
            return;
        }

        logger.warn("Erro na conexão SSE de sincronização de cache:", event);
        // Browser gerencia reconexão automática do EventSource.
    });

    return () => {
        encerradoManualmente = true;
        source.close();
    };
}
