import {ref} from "vue";
import {useSubprocessoStore} from "@/stores/subprocesso";
import {
    buscarContextoCadastroAtividades as serviceBuscarContextoCadastroAtividades,
    buscarContextoCadastroAtividadesPorProcessoEUnidade as serviceBuscarContextoCadastroAtividadesPorProcessoEUnidade,
} from "@/services/subprocessoService";
import type {
    PermissoesSubprocesso,
    SituacaoSubprocesso,
    SubprocessoDetalhe,
} from "@/types/tipos";
import {normalizeError} from "@/utils/apiError";

// Mantemos o estado local para compatibilidade de escrita direta que as views fazem,
// mas as funções de busca agora usam a store para dedupe.
const subprocessoDetalhe = ref<SubprocessoDetalhe | null>(null);

export function useSubprocessos() {
    const store = useSubprocessoStore();

    function limparErroIntegracao() {
        store.erroIntegracaoContexto = null;
    }

    function registrarErroIntegracao(erro: unknown) {
        store.erroIntegracaoContexto = normalizeError(erro);
        return null;
    }

    function sincronizarDetalhes(detalhes: SubprocessoDetalhe) {
        subprocessoDetalhe.value = detalhes;
    }

    async function buscarSubprocessoDetalhe(codigo: number, limparAntes = true): Promise<SubprocessoDetalhe | null> {
        if (limparAntes) subprocessoDetalhe.value = null;
        const contexto = await store.garantirContextoEdicao(codigo, limparAntes);
        if (contexto) {
            sincronizarDetalhes(contexto.detalhes);
        } else if (limparAntes) {
            subprocessoDetalhe.value = null;
        }
        return subprocessoDetalhe.value;
    }

    async function buscarSubprocessoPorProcessoEUnidade(codProcesso: number, siglaUnidade: string): Promise<number | null> {
        try {
            const res = await store.garantirContextoEdicaoPorProcessoEUnidade(codProcesso, siglaUnidade, true);
            if (res) {
                sincronizarDetalhes(res.contexto.detalhes);
                return res.codigo;
            }
        } catch {
            return null;
        }
        return null;
    }

    async function buscarContextoEdicao(codigo: number) {
        subprocessoDetalhe.value = null;
        const contexto = await store.garantirContextoEdicao(codigo, true);
        if (contexto) {
            sincronizarDetalhes(contexto.detalhes);
        }
        return contexto;
    }

    async function buscarContextoEdicaoPorProcessoEUnidade(codProcesso: number, siglaUnidade: string) {
        subprocessoDetalhe.value = null;
        const res = await store.garantirContextoEdicaoPorProcessoEUnidade(codProcesso, siglaUnidade, true);
        if (res) {
            sincronizarDetalhes(res.contexto.detalhes);
            return res.contexto;
        }
        return null;
    }

    async function buscarContextoCadastroAtividades(codigo: number) {
        subprocessoDetalhe.value = null;
        limparErroIntegracao();
        try {
            const data = await serviceBuscarContextoCadastroAtividades(codigo);
            sincronizarDetalhes(data.detalhes);
            return data;
        } catch (erro) {
            return registrarErroIntegracao(erro);
        }
    }

    async function buscarContextoCadastroAtividadesPorProcessoEUnidade(codProcesso: number, siglaUnidade: string) {
        subprocessoDetalhe.value = null;
        limparErroIntegracao();
        try {
            const data = await serviceBuscarContextoCadastroAtividadesPorProcessoEUnidade(codProcesso, siglaUnidade);
            sincronizarDetalhes(data.detalhes);
            return data;
        } catch (erro) {
            return registrarErroIntegracao(erro);
        }
    }

    function atualizarStatusLocal(status: {
        codigo: number;
        situacao: SituacaoSubprocesso;
        permissoes?: PermissoesSubprocesso;
    }) {
        if (!subprocessoDetalhe.value || subprocessoDetalhe.value.codigo !== status.codigo) return;

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
