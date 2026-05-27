import {computed} from "vue";
import {useMutation, useQuery, useQueryCache} from "@pinia/colada";
import {
    buscarUrlLeitorEmailTestes,
    compararNotificacoes,
    listarNotificacoesAdmin,
    reenviarNotificacao,
    type Notificacao,
    type ReenvioNotificacaoResponse,
} from "@/services/notificacaoService";
import {ehModoProducao} from "@/utils/ambiente";

export const CHAVE_QUERY_NOTIFICACOES_ADMIN = ["notificacoes-admin"] as const;
export const CHAVE_QUERY_LEITOR_EMAIL_TESTES = ["leitor-email-testes"] as const;

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

export function useUrlLeitorEmailTestesQuery() {
    return useQuery<string | null>({
        key: CHAVE_QUERY_LEITOR_EMAIL_TESTES,
        query: () => buscarUrlLeitorEmailTestes(),
        enabled: () => !ehModoProducao(),
        staleTime: Infinity,
    });
}

export function useReenvioNotificacaoMutation() {
    return useMutation<ReenvioNotificacaoResponse, number, Error>({
        mutation: (codigoNotificacao) => reenviarNotificacao(codigoNotificacao),
        onSuccess: () => {
            useQueryCache().invalidateQueries({key: CHAVE_QUERY_NOTIFICACOES_ADMIN});
        },
    });
}
