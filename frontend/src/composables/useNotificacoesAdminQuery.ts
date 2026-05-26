import {computed} from "vue";
import {useQuery} from "@pinia/colada";
import {compararNotificacoes, listarNotificacoesAdmin, type Notificacao} from "@/services/notificacaoService";

export const CHAVE_QUERY_NOTIFICACOES_ADMIN = ["notificacoes-admin"] as const;

export function useNotificacoesAdminQuery() {
    const query = useQuery<Notificacao[]>({
        key: CHAVE_QUERY_NOTIFICACOES_ADMIN,
        query: () => listarNotificacoesAdmin(),
        staleTime: Infinity,
    });

    const itensOrdenados = computed(() => [...(query.data.value ?? [])].sort(compararNotificacoes));

    return {
        ...query,
        itensOrdenados,
    };
}
