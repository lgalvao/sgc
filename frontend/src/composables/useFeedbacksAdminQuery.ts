import {useQuery} from "@pinia/colada";
import {usePerfilStore} from "@/stores/perfil";
import {listarFeedbacksAdmin, type FeedbackAdmin} from "@/services/feedbackAdminService";

export const CHAVE_QUERY_FEEDBACKS_ADMIN = ["feedbacks-admin"] as const;

export function useFeedbacksAdminQuery() {
    const perfilStore = usePerfilStore();
    return useQuery<FeedbackAdmin[]>({
        key: CHAVE_QUERY_FEEDBACKS_ADMIN,
        query: () => listarFeedbacksAdmin(),
        enabled: () => !!perfilStore.perfilSelecionado,
        staleTime: Infinity,
    });
}
