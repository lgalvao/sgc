import {computed, ref} from "vue";
import {useSubprocessoStore} from "@/stores/subprocesso";
import {useMapas} from "@/composables/useMapas";
import {
    buscarContextoCadastroAtividades as serviceBuscarContextoCadastroAtividades,
    buscarContextoCadastroAtividadesPorProcessoEUnidade as serviceBuscarContextoCadastroAtividadesPorProcessoEUnidade,
} from "@/services/subprocessoService";
import type {
    PermissoesSubprocesso,
    SituacaoSubprocesso,
    SubprocessoDetalhe,
} from "@/types/tipos";
import {useErrorHandler} from "@/composables/useErrorHandler";

// Mantemos o estado local para compatibilidade de escrita direta que as views fazem,
// mas as funções de busca agora usam a store para dedupe.
const subprocessoDetalhe = ref<SubprocessoDetalhe | null>(null);
const {lastError, clearError, withErrorHandling} = useErrorHandler();

export function useSubprocessos() {
    const store = useSubprocessoStore();

    const subprocessoDetalheProxy = computed<SubprocessoDetalhe | null>({
        get: () => subprocessoDetalhe.value,
        set: (v) => { subprocessoDetalhe.value = v; }
    });

    const lastErrorProxy = computed<typeof lastError.value>({
        get: () => store.erroIntegracaoContexto,
        set: (v) => { store.erroIntegracaoContexto = v; }
    });

    async function buscarSubprocessoDetalhe(codigo: number, limparAntes = true): Promise<SubprocessoDetalhe | null> {
        if (limparAntes) subprocessoDetalhe.value = null;
        try {
            const contexto = await store.garantirContextoEdicao(codigo, limparAntes);
            if (contexto) {
                subprocessoDetalhe.value = contexto.detalhes;
            }
        } catch {
            if (limparAntes) subprocessoDetalhe.value = null;
        }
        return subprocessoDetalhe.value;
    }

    async function buscarSubprocessoPorProcessoEUnidade(codProcesso: number, siglaUnidade: string): Promise<number | null> {
        try {
            const res = await store.garantirContextoEdicaoPorProcessoEUnidade(codProcesso, siglaUnidade, true);
            if (res) {
                subprocessoDetalhe.value = res.contexto.detalhes;
                return res.codigo;
            }
        } catch {
            return null;
        }
        return null;
    }

    async function buscarContextoEdicao(codigo: number) {
        subprocessoDetalhe.value = null;
        try {
            const contexto = await store.garantirContextoEdicao(codigo, true);
            if (contexto) {
                subprocessoDetalhe.value = contexto.detalhes;
                const {mapaCompleto} = useMapas();
                mapaCompleto.value = contexto.mapa;
            }
            return contexto;
        } catch {
            return null;
        }
    }

    async function buscarContextoEdicaoPorProcessoEUnidade(codProcesso: number, siglaUnidade: string) {
        subprocessoDetalhe.value = null;
        try {
            const res = await store.garantirContextoEdicaoPorProcessoEUnidade(codProcesso, siglaUnidade, true);
            if (res) {
                subprocessoDetalhe.value = res.contexto.detalhes;
                const {mapaCompleto} = useMapas();
                mapaCompleto.value = res.contexto.mapa;
                return res.contexto;
            }
        } catch {
            return null;
        }
        return null;
    }

    async function buscarContextoCadastroAtividades(codigo: number) {
        subprocessoDetalhe.value = null;
        return withErrorHandling(async () => {
            const data = await serviceBuscarContextoCadastroAtividades(codigo);
            subprocessoDetalhe.value = data.detalhes;
            // Opcionalmente sincroniza a store se quisermos que a store também guarde isso
            store.contextoEdicao = { detalhes: data.detalhes, mapa: data.mapa } as any;
            return data;
        });
    }

    async function buscarContextoCadastroAtividadesPorProcessoEUnidade(codProcesso: number, siglaUnidade: string) {
        subprocessoDetalhe.value = null;
        return withErrorHandling(async () => {
            const data = await serviceBuscarContextoCadastroAtividadesPorProcessoEUnidade(codProcesso, siglaUnidade);
            subprocessoDetalhe.value = data.detalhes;
            store.contextoEdicao = { detalhes: data.detalhes, mapa: data.mapa } as any;
            return data;
        });
    }

    function atualizarStatusLocal(status: {
        codigo: number;
        situacao: SituacaoSubprocesso;
        permissoes?: PermissoesSubprocesso;
    }) {
        if (!subprocessoDetalhe.value) return;

        subprocessoDetalhe.value = {
            ...subprocessoDetalhe.value,
            situacao: status.situacao,
            permissoes: status.permissoes ?? subprocessoDetalhe.value.permissoes,
        };
        
        if (store.contextoEdicao && store.contextoEdicao.detalhes.codigo === status.codigo) {
            store.contextoEdicao.detalhes.situacao = status.situacao;
            if (status.permissoes) {
                store.contextoEdicao.detalhes.permissoes = status.permissoes;
            }
        }
    }

    return {
        get subprocessoDetalhe() { return subprocessoDetalhe.value; },
        set subprocessoDetalhe(v) { subprocessoDetalhe.value = v; },
        get lastError() { return store.erroIntegracaoContexto; },
        set lastError(v) { store.erroIntegracaoContexto = v; },
        clearError: () => { store.erroIntegracaoContexto = null; },
        buscarSubprocessoDetalhe,
        buscarContextoEdicao,
        buscarContextoEdicaoPorProcessoEUnidade,
        buscarContextoCadastroAtividades,
        buscarContextoCadastroAtividadesPorProcessoEUnidade,
        buscarSubprocessoPorProcessoEUnidade,
        atualizarStatusLocal,
        invalidar: () => store.invalidar()
    };
}
