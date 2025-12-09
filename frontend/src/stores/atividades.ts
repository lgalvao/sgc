import {defineStore} from "pinia";
import {computed, ref} from "vue";
import * as atividadeService from "@/services/atividadeService";
import * as subprocessoService from "@/services/subprocessoService";
import {useFeedbackStore} from "@/stores/feedback";
import type {Atividade, Conhecimento, CriarAtividadeRequest, CriarConhecimentoRequest,} from "@/types/tipos";


export const useAtividadesStore = defineStore("atividades", () => {
    const atividadesPorSubprocesso = ref(new Map<number, Atividade[]>());
    const feedbackStore = useFeedbackStore();

    const obterAtividadesPorSubprocesso = computed(
        () =>
            (codSubrocesso: number): Atividade[] => {
                return atividadesPorSubprocesso.value.get(codSubrocesso) || [];
            },
    );

    async function buscarAtividadesParaSubprocesso(codSubrocesso: number) {
        try {
            const atividades = await subprocessoService.listarAtividades(codSubrocesso);
            atividadesPorSubprocesso.value.set(codSubrocesso, atividades);
        } catch (error) {
            feedbackStore.show(
                "Erro ao buscar atividades",
                "Não foi possível carregar as atividades para o subprocesso.",
                "danger"
            );
            atividadesPorSubprocesso.value.set(codSubrocesso, []);
            throw error;
        }
    }

    async function adicionarAtividade(
        codSubprocesso: number,
        codMapa: number,
        request: CriarAtividadeRequest,
    ) {
        try {
            const response = await atividadeService.criarAtividade(request, codMapa);
            await buscarAtividadesParaSubprocesso(codSubprocesso);
            return response.subprocesso; // Retorna status do subprocesso
        } catch (error) {
            feedbackStore.show("Erro ao adicionar atividade", "Não foi possível adicionar a atividade.", "danger");
            throw error;
        }
    }

    async function removerAtividade(codSubrocesso: number, atividadeId: number) {
        try {
            const response = await atividadeService.excluirAtividade(atividadeId);
            await buscarAtividadesParaSubprocesso(codSubrocesso);
            return response.subprocesso; // Retorna status do subprocesso
        } catch (error) {
            feedbackStore.show("Erro ao remover atividade", "Não foi possível remover a atividade.", "danger");
            throw error;
        }
    }

    async function adicionarConhecimento(
        codSubrocesso: number,
        atividadeId: number,
        request: CriarConhecimentoRequest,
    ) {
        try {
            const response = await atividadeService.criarConhecimento(atividadeId, request);
            await buscarAtividadesParaSubprocesso(codSubrocesso);
            return response.subprocesso; // Retorna status do subprocesso
        } catch (error) {
            feedbackStore.show("Erro ao adicionar conhecimento", "Não foi possível adicionar o conhecimento.", "danger");
            throw error;
        }
    }

    async function removerConhecimento(
        codSubrocesso: number,
        atividadeId: number,
        conhecimentoId: number,
    ) {
        try {
            const response = await atividadeService.excluirConhecimento(atividadeId, conhecimentoId);
            await buscarAtividadesParaSubprocesso(codSubrocesso);
            return response.subprocesso; // Retorna status do subprocesso
        } catch (error) {
            feedbackStore.show("Erro ao remover conhecimento", "Não foi possível remover o conhecimento.", "danger");
            throw error;
        }
    }

    async function importarAtividades(
        codSubrocessoDestino: number,
        codSubrocessoOrigem: number,
    ) {
        try {
            await subprocessoService.importarAtividades(codSubrocessoDestino, codSubrocessoOrigem);
            await buscarAtividadesParaSubprocesso(codSubrocessoDestino);
        } catch (error) {
            feedbackStore.show("Erro ao importar atividades", "Não foi possível importar as atividades.", "danger");
            throw error;
        }
    }

    async function atualizarAtividade(
        codSubrocesso: number,
        atividadeId: number,
        data: Atividade,
    ) {
        try {
            const response = await atividadeService.atualizarAtividade(atividadeId, data);
            await buscarAtividadesParaSubprocesso(codSubrocesso);
            return response.subprocesso; // Retorna status do subprocesso
        } catch (error) {
            feedbackStore.show("Erro ao atualizar atividade", "Não foi possível atualizar a atividade.", "danger");
            throw error;
        }
    }

    async function atualizarConhecimento(
        codSubrocesso: number,
        atividadeId: number,
        conhecimentoId: number,
        data: Conhecimento,
    ) {
        try {
            const response = await atividadeService.atualizarConhecimento(atividadeId, conhecimentoId, data);
            await buscarAtividadesParaSubprocesso(codSubrocesso);
            return response.subprocesso; // Retorna status do subprocesso
        } catch (error) {
            feedbackStore.show("Erro ao atualizar conhecimento", "Não foi possível atualizar o conhecimento.", "danger");
            throw error;
        }
    }

    return {
        atividadesPorSubprocesso,
        obterAtividadesPorSubprocesso,
        buscarAtividadesParaSubprocesso,
        adicionarAtividade,
        removerAtividade,
        adicionarConhecimento,
        removerConhecimento,
        importarAtividades,
        atualizarAtividade,
        atualizarConhecimento,
    };
});
