import {defineStore} from "pinia";
import {computed, ref} from "vue";
import * as atividadeService from "@/services/atividadeService";
import * as subprocessoService from "@/services/subprocessoService";
import type {Atividade, Conhecimento, CriarAtividadeRequest, CriarConhecimentoRequest,} from "@/types/tipos";
import {useSubprocessosStore} from "@/stores/subprocessos";
import {useErrorHandler} from "@/composables/useErrorHandler";

export const useAtividadesStore = defineStore("atividades", () => {
    const atividadesPorSubprocesso = ref(new Map<number, Atividade[]>());
    const { lastError, clearError, withErrorHandling } = useErrorHandler();

    const obterAtividadesPorSubprocesso = computed(
        () =>
            (codSubrocesso: number): Atividade[] => {
                return atividadesPorSubprocesso.value.get(codSubrocesso) || [];
            },
    );

    function setAtividadesParaSubprocesso(codSubprocesso: number, atividades: Atividade[]) {
        atividadesPorSubprocesso.value.set(codSubprocesso, atividades);
    }

    async function buscarAtividadesParaSubprocesso(codSubrocesso: number) {
        return withErrorHandling(async () => {
            const atividades = await subprocessoService.listarAtividades(codSubrocesso);
            atividadesPorSubprocesso.value.set(codSubrocesso, atividades);
        }, () => {
            atividadesPorSubprocesso.value.set(codSubrocesso, []);
        });
    }

    async function adicionarAtividade(
        codSubprocesso: number,
        codMapa: number,
        request: CriarAtividadeRequest,
    ) {
        return withErrorHandling(async () => {
            const response = await atividadeService.criarAtividade(request, codMapa);
            await buscarAtividadesParaSubprocesso(codSubprocesso);
            // Atualiza o detalhe do subprocesso para refletir mudanças de estado
            const subprocessosStore = useSubprocessosStore();
            await subprocessosStore.buscarSubprocessoDetalhe(codSubprocesso);
            return response.subprocesso; // Retorna status do subprocesso
        });
    }

    async function removerAtividade(codSubrocesso: number, atividadeId: number) {
        return withErrorHandling(async () => {
            const response = await atividadeService.excluirAtividade(atividadeId);
            await buscarAtividadesParaSubprocesso(codSubrocesso);
            // Atualiza o detalhe do subprocesso para refletir mudanças de estado
            const subprocessosStore = useSubprocessosStore();
            await subprocessosStore.buscarSubprocessoDetalhe(codSubrocesso);
            return response.subprocesso; // Retorna status do subprocesso
        });
    }

    async function adicionarConhecimento(
        codSubrocesso: number,
        atividadeId: number,
        request: CriarConhecimentoRequest,
    ) {
        return withErrorHandling(async () => {
            const response = await atividadeService.criarConhecimento(atividadeId, request);
            await buscarAtividadesParaSubprocesso(codSubrocesso);
            // Atualiza o detalhe do subprocesso para refletir mudanças de estado
            const subprocessosStore = useSubprocessosStore();
            await subprocessosStore.buscarSubprocessoDetalhe(codSubrocesso);
            return response.subprocesso; // Retorna status do subprocesso
        });
    }

    async function removerConhecimento(
        codSubrocesso: number,
        atividadeId: number,
        conhecimentoId: number,
    ) {
        return withErrorHandling(async () => {
            const response = await atividadeService.excluirConhecimento(atividadeId, conhecimentoId);
            await buscarAtividadesParaSubprocesso(codSubrocesso);
            // Atualiza o detalhe do subprocesso para refletir mudanças de estado
            const subprocessosStore = useSubprocessosStore();
            await subprocessosStore.buscarSubprocessoDetalhe(codSubrocesso);
            return response.subprocesso; // Retorna status do subprocesso
        });
    }

    async function importarAtividades(
        codSubrocessoDestino: number,
        codSubrocessoOrigem: number,
    ) {
        return withErrorHandling(async () => {
            await subprocessoService.importarAtividades(codSubrocessoDestino, codSubrocessoOrigem);
            await buscarAtividadesParaSubprocesso(codSubrocessoDestino);
            // Atualiza o detalhe do subprocesso para refletir mudanças de estado
            const subprocessosStore = useSubprocessosStore();
            await subprocessosStore.buscarSubprocessoDetalhe(codSubrocessoDestino);
        });
    }

    async function atualizarAtividade(
        codSubrocesso: number,
        atividadeId: number,
        data: Atividade,
    ) {
        return withErrorHandling(async () => {
            const response = await atividadeService.atualizarAtividade(atividadeId, data);
            await buscarAtividadesParaSubprocesso(codSubrocesso);
            // Atualiza o detalhe do subprocesso para refletir mudanças de estado
            const subprocessosStore = useSubprocessosStore();
            await subprocessosStore.buscarSubprocessoDetalhe(codSubrocesso);
            return response.subprocesso; // Retorna status do subprocesso
        });
    }

    async function atualizarConhecimento(
        codSubrocesso: number,
        atividadeId: number,
        conhecimentoId: number,
        data: Conhecimento,
    ) {
        return withErrorHandling(async () => {
            const response = await atividadeService.atualizarConhecimento(atividadeId, conhecimentoId, data);
            await buscarAtividadesParaSubprocesso(codSubrocesso);
            // Atualiza o detalhe do subprocesso para refletir mudanças de estado
            const subprocessosStore = useSubprocessosStore();
            await subprocessosStore.buscarSubprocessoDetalhe(codSubrocesso);
            return response.subprocesso; // Retorna status do subprocesso
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
