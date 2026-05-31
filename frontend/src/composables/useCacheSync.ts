import {useInvalidacaoNavegacao} from "@/composables/useInvalidacaoNavegacao";
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
    const {atualizarDadosOrganizacionais} = useInvalidacaoNavegacao();
    const sincronizarMudancaOrganizacional = () => atualizarDadosOrganizacionais();
    let encerradoManualmente = false;
    let source: EventSource | null = null;
    const aoDarErro = (event: Event) => {
        if (encerradoManualmente || !source) {
            return;
        }
        if (source.readyState !== EventSource.OPEN) {
            return;
        }
        logger.warn("Erro na conexão SSE de sincronização de cache:", event);
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
        source.addEventListener(EVENTO_CACHE_ATUALIZADO, sincronizarMudancaOrganizacional);
        source.addEventListener("error", aoDarErro);
    };
    const aoOcultarPagina = () => {
        if (encerradoManualmente) {
            return;
        }
        desconectar();
    };
    conectar();
    globalThis.window?.addEventListener("pagehide", aoOcultarPagina);
    globalThis.window?.addEventListener("beforeunload", aoOcultarPagina);
    globalThis.window?.addEventListener("pageshow", conectar);

    return () => {
        encerradoManualmente = true;
        globalThis.window?.removeEventListener("pagehide", aoOcultarPagina);
        globalThis.window?.removeEventListener("beforeunload", aoOcultarPagina);
        globalThis.window?.removeEventListener("pageshow", conectar);
        desconectar();
    };
}
