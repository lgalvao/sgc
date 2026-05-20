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
    let encerradoManualmente = false;
    let source: EventSource | null = null;

    const handleAtualizacaoCache = () => {
        unidadeStore.invalidar();
        organizacaoStore.invalidar();
        painelStore.invalidar();
    };

    const handleErro = (event: Event) => {
        if (encerradoManualmente || !source) {
            return;
        }

        if (source.readyState !== EventSource.OPEN) {
            return;
        }

        logger.warn("Erro na conexão SSE de sincronização de cache:", event);
        // Browser gerencia reconexão automática do EventSource.
    };

    const desconectar = () => {
        source?.close();
        source = null;
    };

    const conectar = () => {
        if (encerradoManualmente || source) {
            return;
        }

        source = new EventSource("/api/eventos");
        source.addEventListener(EVENTO_CACHE_ATUALIZADO, handleAtualizacaoCache);
        source.addEventListener("error", handleErro);
    };

    const handlePageHide = () => {
        if (encerradoManualmente) {
            return;
        }
        desconectar();
    };

    const handlePageShow = () => {
        conectar();
    };

    conectar();
    globalThis.window?.addEventListener("pagehide", handlePageHide);
    globalThis.window?.addEventListener("beforeunload", handlePageHide);
    globalThis.window?.addEventListener("pageshow", handlePageShow);

    return () => {
        encerradoManualmente = true;
        globalThis.window?.removeEventListener("pagehide", handlePageHide);
        globalThis.window?.removeEventListener("beforeunload", handlePageHide);
        globalThis.window?.removeEventListener("pageshow", handlePageShow);
        desconectar();
    };
}
