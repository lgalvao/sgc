import {defineStore} from "pinia";
import {computed, ref} from "vue";
import {mapMapaVisualizacaoToAtividades} from "@/mappers/mapas";
import * as atividadeService from "@/services/atividadeService";
import * as mapaService from "@/services/mapaService";
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
            const mapa = await mapaService.obterMapaVisualizacao(codSubrocesso);
            const atividades = mapMapaVisualizacaoToAtividades(mapa);
            atividadesPorSubprocesso.value.set(codSubrocesso, atividades);
        } catch (error) {
            feedbackStore.show(
                "Erro ao buscar atividades",
                "Não foi possível carregar as atividades para o subprocesso.",
                "danger"
            );
            atividadesPorSubprocesso.value.set(codSubrocesso, []);
            throw error; // Re-throw to propagate error to UI
        }
    }

    async function adicionarAtividade(
        codSubprocesso: number,
        codMapa: number,
        request: CriarAtividadeRequest,
    ) {
        try {
            const novaAtividade = await atividadeService.criarAtividade(
                request,
                codMapa,
            );
            if (novaAtividade) {
                const atividades =
                    atividadesPorSubprocesso.value.get(codSubprocesso) || [];
                atividades.push(novaAtividade);
                atividadesPorSubprocesso.value.set(codSubprocesso, atividades);
            }
            // Re-fetch to ensure consistency with backend
            await buscarAtividadesParaSubprocesso(codSubprocesso);
        } catch (error) {
            feedbackStore.show("Erro ao adicionar atividade", "Não foi possível adicionar a atividade.", "danger");
            throw error;
        }
    }

    async function removerAtividade(codSubrocesso: number, atividadeId: number) {
        try {
            await atividadeService.excluirAtividade(atividadeId);
            atividadesPorSubprocesso.value.set(
                codSubrocesso,
                (atividadesPorSubprocesso.value.get(codSubrocesso) || []).filter(
                    (a) => a.codigo !== atividadeId,
                ),
            );
            // Re-fetch to ensure data consistency
            await buscarAtividadesParaSubprocesso(codSubrocesso);
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
            const novoConhecimento = await atividadeService.criarConhecimento(
                atividadeId,
                request,
            );
            const updatedAtividades = (
                atividadesPorSubprocesso.value.get(codSubrocesso) || []
            ).map((a) => {
                if (a.codigo === atividadeId) {
                    return {
                        ...a,
                        conhecimentos: [...a.conhecimentos, novoConhecimento],
                    };
                }
                return a;
            });
            atividadesPorSubprocesso.value.set(codSubrocesso, updatedAtividades);
            await buscarAtividadesParaSubprocesso(codSubrocesso);
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
            await atividadeService.excluirConhecimento(atividadeId, conhecimentoId);
            const atividades =
                (atividadesPorSubprocesso.value.get(codSubrocesso) || []).map(
                    (a) => {
                        if (a.codigo === atividadeId) {
                            return {
                                ...a,
                                conhecimentos: a.conhecimentos.filter(
                                    (c) => c.id !== conhecimentoId,
                                ),
                            };
                        }
                        return a;
                    },
                );
            atividadesPorSubprocesso.value.set(codSubrocesso, atividades);
            await buscarAtividadesParaSubprocesso(codSubrocesso);
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
            await subprocessoService.importarAtividades(
                codSubrocessoDestino,
                codSubrocessoOrigem,
            );
            // Recarregar as atividades do subprocesso de destino para refletir a importação
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
            const atividadeAtualizada = await atividadeService.atualizarAtividade(
                atividadeId,
                data,
            );
            const atividadesAtualizadas = (
                atividadesPorSubprocesso.value.get(codSubrocesso) || []
            ).map((a) => (a.codigo === atividadeId ? atividadeAtualizada : a));
            atividadesPorSubprocesso.value.set(codSubrocesso, atividadesAtualizadas);
            await buscarAtividadesParaSubprocesso(codSubrocesso);
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
            const conhecimentoAtualizado =
                await atividadeService.atualizarConhecimento(
                    atividadeId,
                    conhecimentoId,
                    data,
                );
            const atividadesAtualizadas = (
                atividadesPorSubprocesso.value.get(codSubrocesso) || []
            ).map((atividade) => {
                if (atividade.codigo === atividadeId) {
                    return {
                        ...atividade,
                        conhecimentos: atividade.conhecimentos.map((c) =>
                            c.id === conhecimentoId ? conhecimentoAtualizado : c,
                        ),
                    };
                }
                return atividade;
            });
            atividadesPorSubprocesso.value.set(codSubrocesso, atividadesAtualizadas);
            await buscarAtividadesParaSubprocesso(codSubrocesso);
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
