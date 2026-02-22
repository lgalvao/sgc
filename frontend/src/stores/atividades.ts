import {defineStore} from "pinia";
import {ref} from "vue";
import * as atividadeService from "@/services/atividadeService";
import * as subprocessoService from "@/services/subprocessoService";
import type {
    Atividade,
    AtividadeOperacaoResponse,
    Conhecimento,
    CriarAtividadeRequest,
    CriarConhecimentoRequest,
} from "@/types/tipos";
import {useSubprocessosStore} from "@/stores/subprocessos";
import {useErrorHandler} from "@/composables/useErrorHandler";
import {mapAtividadeVisualizacaoToModel} from "@/mappers/atividades";

export const useAtividadesStore = defineStore("atividades", () => {
    const atividadesPorSubprocesso = ref(new Map<number, Atividade[]>());
    const { lastError, clearError, withErrorHandling } = useErrorHandler();

    function obterAtividadesPorSubprocesso(codSubprocesso: number): Atividade[] {
        return atividadesPorSubprocesso.value.get(codSubprocesso) || [];
    }

    function setAtividadesParaSubprocesso(codSubprocesso: number, atividades: Atividade[]) {
        atividadesPorSubprocesso.value.set(codSubprocesso, atividades);
    }

    /**
     * Atualiza o estado local com dados retornados pela operação CRUD.
     * Elimina a necessidade de chamadas extras ao backend.
     */
    async function atualizarDadosLocais(codSubprocesso: number, response: AtividadeOperacaoResponse) {
        // Atualizar lista de atividades no cache local
        if (response.atividadesAtualizadas) {
            const atividades = response.atividadesAtualizadas
                .map(mapAtividadeVisualizacaoToModel)
                .filter((a): a is Atividade => a !== null);
            atividadesPorSubprocesso.value.set(codSubprocesso, atividades);
        }
        
        // Atualizar status do subprocesso
        const subprocessosStore = useSubprocessosStore();
        subprocessosStore.atualizarStatusLocal(response.subprocesso);
        
    }

    async function buscarAtividadesParaSubprocesso(codSubprocesso: number) {
        return withErrorHandling(async () => {
            const atividades = await subprocessoService.listarAtividades(codSubprocesso);
            atividadesPorSubprocesso.value.set(codSubprocesso, atividades);
        }, () => {
            atividadesPorSubprocesso.value.set(codSubprocesso, []);
        });
    }

    async function adicionarAtividade(
        codSubprocesso: number,
        codMapa: number,
        request: CriarAtividadeRequest,
    ) {
        return withErrorHandling(async () => {
            const response = await atividadeService.criarAtividade(request, codMapa);
            // ✅ Usar dados da resposta para atualizar estado local (elimina 2 chamadas HTTP)
            await atualizarDadosLocais(codSubprocesso, response);
            return response.subprocesso;
        });
    }

    async function removerAtividade(codSubprocesso: number, atividadeId: number) {
        return withErrorHandling(async () => {
            const response = await atividadeService.excluirAtividade(atividadeId);
            // ✅ Usar dados da resposta para atualizar estado local (elimina 2 chamadas HTTP)
            await atualizarDadosLocais(codSubprocesso, response);
            return response.subprocesso;
        });
    }

    async function adicionarConhecimento(
        codSubprocesso: number,
        atividadeId: number,
        request: CriarConhecimentoRequest,
    ) {
        return withErrorHandling(async () => {
            const response = await atividadeService.criarConhecimento(atividadeId, request);
            // ✅ Usar dados da resposta para atualizar estado local (elimina 2 chamadas HTTP)
            await atualizarDadosLocais(codSubprocesso, response);
            return response.subprocesso;
        });
    }

    async function removerConhecimento(
        codSubprocesso: number,
        atividadeId: number,
        conhecimentoId: number,
    ) {
        return withErrorHandling(async () => {
            const response = await atividadeService.excluirConhecimento(atividadeId, conhecimentoId);
            // ✅ Usar dados da resposta para atualizar estado local (elimina 2 chamadas HTTP)
            await atualizarDadosLocais(codSubprocesso, response);
            return response.subprocesso;
        });
    }

    async function importarAtividades(
        codSubprocessoDestino: number,
        codSubprocessoOrigem: number,
    ) {
        return withErrorHandling(async () => {
            // Nota: importarAtividades não retorna AtividadeOperacaoResponse,
            // então mantemos as chamadas de reload aqui
            await subprocessoService.importarAtividades(codSubprocessoDestino, codSubprocessoOrigem);
            await buscarAtividadesParaSubprocesso(codSubprocessoDestino);
            const subprocessosStore = useSubprocessosStore();
            await subprocessosStore.buscarSubprocessoDetalhe(codSubprocessoDestino);
        });
    }

    async function atualizarAtividade(
        codSubprocesso: number,
        atividadeId: number,
        data: Atividade,
    ) {
        return withErrorHandling(async () => {
            const response = await atividadeService.atualizarAtividade(atividadeId, data);
            // ✅ Usar dados da resposta para atualizar estado local (elimina 2 chamadas HTTP)
            await atualizarDadosLocais(codSubprocesso, response);
            return response.subprocesso;
        });
    }

    async function atualizarConhecimento(
        codSubprocesso: number,
        atividadeId: number,
        conhecimentoId: number,
        data: Conhecimento,
    ) {
        return withErrorHandling(async () => {
            const response = await atividadeService.atualizarConhecimento(atividadeId, conhecimentoId, data);
            // ✅ Usar dados da resposta para atualizar estado local (elimina 2 chamadas HTTP)
            await atualizarDadosLocais(codSubprocesso, response);
            return response.subprocesso;
        });
    }

    return {
        atividadesPorSubprocesso,
        lastError,
        obterAtividadesPorSubprocesso,
        setAtividadesParaSubprocesso,
        buscarAtividadesParaSubprocesso,
        adicionarAtividade,
        removerAtividade,
        adicionarConhecimento,
        removerConhecimento,
        importarAtividades,
        atualizarAtividade,
        atualizarConhecimento,
        clearError
    };
});