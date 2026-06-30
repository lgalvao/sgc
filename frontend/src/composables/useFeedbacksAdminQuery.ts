// @sgc-auditoria ignorar: arquivoMinusculo | padrão Pinia Colada: arquivo de domínio com chave de query — pequeno por design
import {useQuery} from "@pinia/colada";
import {STALE_TIME_CONTROLADO_POR_INVALIDACAO} from "@/composables/cachePolicy";
import {usePerfilStore} from "@/stores/perfil";
import {type FeedbackAdmin, listarFeedbacksAdmin} from "@/services/feedbackAdminService";

export const CHAVE_QUERY_FEEDBACKS_ADMIN = ["feedbacks-admin"] as const;

export function useFeedbacksAdminQuery() {
    const perfilStore = usePerfilStore();
    return useQuery<FeedbackAdmin[]>({
        key: CHAVE_QUERY_FEEDBACKS_ADMIN,
        query: () => listarFeedbacksAdmin(),
        enabled: () => !!perfilStore.perfilSelecionado,
        staleTime: STALE_TIME_CONTROLADO_POR_INVALIDACAO,
    });
}
