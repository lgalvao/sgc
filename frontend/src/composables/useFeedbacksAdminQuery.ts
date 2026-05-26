import {useQuery} from "@pinia/colada";
import {listarFeedbacksAdmin, type FeedbackAdmin} from "@/services/feedbackAdminService";

export const CHAVE_QUERY_FEEDBACKS_ADMIN = ["feedbacks-admin"] as const;

export function useFeedbacksAdminQuery() {
    return useQuery<FeedbackAdmin[]>({
        key: CHAVE_QUERY_FEEDBACKS_ADMIN,
        query: () => listarFeedbacksAdmin(),
        staleTime: Infinity,
    });
}
