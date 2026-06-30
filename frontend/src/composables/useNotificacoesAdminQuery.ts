import {computed} from "vue";
import {useMutation, useQuery, useQueryCache} from "@pinia/colada";
import {STALE_TIME_CONTROLADO_POR_INVALIDACAO} from "@/composables/cachePolicy";
import {
    buscarUrlLeitorEmailTestes,
    compararNotificacoes,
    listarNotificacoesAdmin,
    type Notificacao,
    reenviarNotificacao,
    type ReenvioNotificacaoResponse,
} from "@/services/notificacaoService";
import {ehModoProducao} from "@/utils/ambiente";
import {usePerfilStore} from "@/stores/perfil";

export const CHAVE_QUERY_NOTIFICACOES_ADMIN = ["notificacoes-admin"] as const;
export const CHAVE_QUERY_LEITOR_EMAIL_TESTES = ["leitor-email-testes"] as const;

export function useNotificacoesAdminQuery() {
    const perfilStore = usePerfilStore();
    const query = useQuery<Notificacao[]>({
        key: CHAVE_QUERY_NOTIFICACOES_ADMIN,
        query: () => listarNotificacoesAdmin(),
        enabled: () => !!perfilStore.perfilSelecionado,
        staleTime: STALE_TIME_CONTROLADO_POR_INVALIDACAO,
    });

    const itensOrdenados = computed(() => (query.data.value ?? []).toSorted(compararNotificacoes));

    return {
        ...query,
        itensOrdenados,
    };
}

export function useUrlLeitorEmailTestesQuery() {
    const perfilStore = usePerfilStore();
    return useQuery<string | null>({
        key: CHAVE_QUERY_LEITOR_EMAIL_TESTES,
        query: () => buscarUrlLeitorEmailTestes(),
        enabled: () => !ehModoProducao() && !!perfilStore.perfilSelecionado,
        staleTime: STALE_TIME_CONTROLADO_POR_INVALIDACAO,
    });
}

export function useReenvioNotificacaoMutation() {
    return useMutation<ReenvioNotificacaoResponse, number>({
        mutation: (codigoNotificacao) => reenviarNotificacao(codigoNotificacao),
        onSuccess: () => {
            void useQueryCache().invalidateQueries({key: CHAVE_QUERY_NOTIFICACOES_ADMIN});
        },
    });
}
