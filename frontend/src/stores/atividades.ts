import {defineStore} from "pinia";
import {computed, ref} from "vue";
import {mapMapaVisualizacaoToAtividades} from "@/mappers/mapas";
import * as atividadeService from "@/services/atividadeService";
import * as mapaService from "@/services/mapaService";
import * as subprocessoService from "@/services/subprocessoService";
import type {Atividade, Conhecimento, CriarAtividadeRequest, CriarConhecimentoRequest,} from "@/types/tipos";
import {useNotificacoesStore} from "./notificacoes";

export const useAtividadesStore = defineStore("atividades", () => {
    const atividadesPorSubprocesso = ref(new Map<number, Atividade[]>());

    const getAtividadesPorSubprocesso = computed(
        () =>
            (codSubrocesso: number): Atividade[] => {
                return atividadesPorSubprocesso.value.get(codSubrocesso) || [];
            },
    );

    async function fetchAtividadesParaSubprocesso(codSubrocesso: number) {
        const notificacoes = useNotificacoesStore();
        try {
            const mapa = await mapaService.obterMapaVisualizacao(codSubrocesso);
            const atividades = mapMapaVisualizacaoToAtividades(mapa);
            atividadesPorSubprocesso.value.set(codSubrocesso, atividades);
        } catch {
            notificacoes.erro(
                "Erro ao buscar atividades",
                "Não foi possível carregar as atividades do subprocesso.",
            );
        }
    }

    async function adicionarAtividade(
        codSubrocesso: number,
        request: CriarAtividadeRequest,
    ) {
        const notificacoes = useNotificacoesStore();
        try {
            // Passa o codSubrocesso para o serviço, que o adicionará ao DTO
            const novaAtividade = await atividadeService.criarAtividade(
                request,
                codSubrocesso,
            );
            const atividades =
                atividadesPorSubprocesso.value.get(codSubrocesso) || [];
            atividades.push(novaAtividade);
            atividadesPorSubprocesso.value.set(codSubrocesso, atividades);
            notificacoes.sucesso(
                "Atividade adicionada",
                "A nova atividade foi adicionada.",
            );
            // Opcional: recarregar para garantir consistência total, mas a adição otimista já ajuda.
            await fetchAtividadesParaSubprocesso(codSubrocesso);
        } catch {
            notificacoes.erro(
                "Erro ao adicionar atividade",
                "Não foi possível salvar a nova atividade.",
            );
        }
    }

    async function removerAtividade(codSubrocesso: number, atividadeId: number) {
        try {
            await atividadeService.excluirAtividade(atividadeId);
            let atividades = atividadesPorSubprocesso.value.get(codSubrocesso) || [];
            atividades = atividades.filter((a) => a.codigo !== atividadeId);
            atividadesPorSubprocesso.value.set(codSubrocesso, atividades);
            useNotificacoesStore().sucesso(
                "Atividade removida",
                "A atividade foi removida.",
            );
        } catch {
            useNotificacoesStore().erro(
                "Erro ao remover atividade",
                "Não foi possível remover a atividade.",
            );
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
            const atividades =
                atividadesPorSubprocesso.value.get(codSubrocesso) || [];
            const atividade = atividades.find((a) => a.codigo === atividadeId);
            if (atividade) {
                atividade.conhecimentos.push(novoConhecimento);
                atividadesPorSubprocesso.value.set(codSubrocesso, atividades);
            }
            useNotificacoesStore().sucesso(
                "Conhecimento adicionado",
                "O novo conhecimento foi adicionado.",
            );
        } catch {
            useNotificacoesStore().erro(
                "Erro ao adicionar conhecimento",
                "Não foi possível salvar o novo conhecimento.",
            );
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
                atividadesPorSubprocesso.value.get(codSubrocesso) || [];
            const atividade = atividades.find((a) => a.codigo === atividadeId);
            if (atividade) {
                atividade.conhecimentos = atividade.conhecimentos.filter(
                    (c) => c.id !== conhecimentoId,
                );
                atividadesPorSubprocesso.value.set(codSubrocesso, atividades);
            }
            useNotificacoesStore().sucesso(
                "Conhecimento removido",
                "O conhecimento foi removido.",
            );
        } catch {
            useNotificacoesStore().erro(
                "Erro ao remover conhecimento",
                "Não foi possível remover o conhecimento.",
            );
        }
    }

    async function importarAtividades(
        codSubrocessoDestino: number,
        codSubrocessoOrigem: number,
    ) {
        const notificacoes = useNotificacoesStore();
        try {
            await subprocessoService.importarAtividades(
                codSubrocessoDestino,
                codSubrocessoOrigem,
            );
            notificacoes.sucesso(
                "Atividades importadas",
                "As atividades foram importadas.",
            );
            // Recarregar as atividades do subprocesso de destino para refletir a importação
            await fetchAtividadesParaSubprocesso(codSubrocessoDestino);
        } catch {
            notificacoes.erro(
                "Erro ao importar",
                "Não foi possível importar as atividades.",
            );
        }
    }

    async function atualizarAtividade(
        codSubrocesso: number,
        atividadeId: number,
        data: Atividade,
    ) {
        const notificacoes = useNotificacoesStore();
        try {
            const atividadeAtualizada = await atividadeService.atualizarAtividade(
                atividadeId,
                data,
            );
            const atividades =
                atividadesPorSubprocesso.value.get(codSubrocesso) || [];
            const index = atividades.findIndex((a) => a.codigo === atividadeId);
            if (index !== -1) {
                atividades[index] = atividadeAtualizada;
                atividadesPorSubprocesso.value.set(codSubrocesso, atividades);
            }
            notificacoes.sucesso(
                "Atividade atualizada",
                "A atividade foi atualizada.",
            );
        } catch {
            notificacoes.erro(
                "Erro ao atualizar",
                "Não foi possível atualizar a atividade.",
            );
        }
    }

    async function atualizarConhecimento(
        codSubrocesso: number,
        atividadeId: number,
        conhecimentoId: number,
        data: Conhecimento,
    ) {
        const notificacoes = useNotificacoesStore();
        try {
            const conhecimentoAtualizado =
                await atividadeService.atualizarConhecimento(
                    atividadeId,
                    conhecimentoId,
                    data,
                );
            const atividades =
                atividadesPorSubprocesso.value.get(codSubrocesso) || [];
            const atividade = atividades.find((a) => a.codigo === atividadeId);
            if (atividade) {
                const index = atividade.conhecimentos.findIndex(
                    (c) => c.id === conhecimentoId,
                );
                if (index !== -1) {
                    atividade.conhecimentos[index] = conhecimentoAtualizado;
                    atividadesPorSubprocesso.value.set(codSubrocesso, atividades);
                }
            }
            notificacoes.sucesso(
                "Conhecimento atualizado",
                "O conhecimento foi atualizado.",
            );
        } catch {
            notificacoes.erro(
                "Erro ao atualizar",
                "Não foi possível atualizar o conhecimento.",
            );
        }
    }

    return {
        atividadesPorSubprocesso,
        getAtividadesPorSubprocesso,
        fetchAtividadesParaSubprocesso,
        adicionarAtividade,
        removerAtividade,
        adicionarConhecimento,
        removerConhecimento,
        importarAtividades,
        atualizarAtividade,
        atualizarConhecimento,
    };
});
