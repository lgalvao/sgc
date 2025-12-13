import {defineStore} from "pinia";
import {computed, ref} from "vue";
import * as atividadeService from "@/services/atividadeService";
import * as subprocessoService from "@/services/subprocessoService";
import {useFeedbackStore} from "@/stores/feedback";
import type {Atividade, Conhecimento, CriarAtividadeRequest, CriarConhecimentoRequest,} from "@/types/tipos";
import { normalizeError, type NormalizedError } from "@/utils/apiError";

export const useAtividadesStore = defineStore("atividades", () => {
    const atividadesPorSubprocesso = ref(new Map<number, Atividade[]>());
    const lastError = ref<NormalizedError | null>(null);
    const feedbackStore = useFeedbackStore();

    function clearError() {
        lastError.value = null;
    }

    const obterAtividadesPorSubprocesso = computed(
        () =>
            (codSubrocesso: number): Atividade[] => {
                return atividadesPorSubprocesso.value.get(codSubrocesso) || [];
            },
    );

    async function buscarAtividadesParaSubprocesso(codSubrocesso: number) {
        lastError.value = null;
        try {
            const atividades = await subprocessoService.listarAtividades(codSubrocesso);
            atividadesPorSubprocesso.value.set(codSubrocesso, atividades);
        } catch (error) {
            lastError.value = normalizeError(error);
            atividadesPorSubprocesso.value.set(codSubrocesso, []);
            throw error;
        }
    }

    async function adicionarAtividade(
        codSubprocesso: number,
        codMapa: number,
        request: CriarAtividadeRequest,
    ) {
        lastError.value = null;
        try {
            const response = await atividadeService.criarAtividade(request, codMapa);
            await buscarAtividadesParaSubprocesso(codSubprocesso);
            return response.subprocesso; // Retorna status do subprocesso
        } catch (error) {
            lastError.value = normalizeError(error);
            throw error;
        }
    }

    async function removerAtividade(codSubrocesso: number, atividadeId: number) {
        lastError.value = null;
        try {
            const response = await atividadeService.excluirAtividade(atividadeId);
            await buscarAtividadesParaSubprocesso(codSubrocesso);
            return response.subprocesso; // Retorna status do subprocesso
        } catch (error) {
            lastError.value = normalizeError(error);
            throw error;
        }
    }

    async function adicionarConhecimento(
        codSubrocesso: number,
        atividadeId: number,
        request: CriarConhecimentoRequest,
    ) {
        lastError.value = null;
        try {
            const response = await atividadeService.criarConhecimento(atividadeId, request);
            await buscarAtividadesParaSubprocesso(codSubrocesso);
            return response.subprocesso; // Retorna status do subprocesso
        } catch (error) {
            lastError.value = normalizeError(error);
            throw error;
        }
    }

    async function removerConhecimento(
        codSubrocesso: number,
        atividadeId: number,
        conhecimentoId: number,
    ) {
        lastError.value = null;
        try {
            const response = await atividadeService.excluirConhecimento(atividadeId, conhecimentoId);
            await buscarAtividadesParaSubprocesso(codSubrocesso);
            return response.subprocesso; // Retorna status do subprocesso
        } catch (error) {
            lastError.value = normalizeError(error);
            throw error;
        }
    }

    async function importarAtividades(
        codSubrocessoDestino: number,
        codSubrocessoOrigem: number,
    ) {
        lastError.value = null;
        try {
            await subprocessoService.importarAtividades(codSubrocessoDestino, codSubrocessoOrigem);
            await buscarAtividadesParaSubprocesso(codSubrocessoDestino);
        } catch (error) {
            lastError.value = normalizeError(error);
            throw error;
        }
    }

    async function atualizarAtividade(
        codSubrocesso: number,
        atividadeId: number,
        data: Atividade,
    ) {
        lastError.value = null;
        try {
            const response = await atividadeService.atualizarAtividade(atividadeId, data);
            await buscarAtividadesParaSubprocesso(codSubrocesso);
            return response.subprocesso; // Retorna status do subprocesso
        } catch (error) {
            lastError.value = normalizeError(error);
            throw error;
        }
    }

    async function atualizarConhecimento(
        codSubrocesso: number,
        atividadeId: number,
        conhecimentoId: number,
        data: Conhecimento,
    ) {
        lastError.value = null;
        try {
            const response = await atividadeService.atualizarConhecimento(atividadeId, conhecimentoId, data);
            await buscarAtividadesParaSubprocesso(codSubrocesso);
            return response.subprocesso; // Retorna status do subprocesso
        } catch (error) {
            lastError.value = normalizeError(error);
            throw error;
        }
    }

    return {
        atividadesPorSubprocesso,
        lastError,
        obterAtividadesPorSubprocesso,
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
