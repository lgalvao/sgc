import {defineStore} from "pinia";
import {computed, ref} from "vue";
import {mapMapaVisualizacaoToAtividades} from "@/mappers/mapas";
import * as atividadeService from "@/services/atividadeService";
import * as mapaService from "@/services/mapaService";
import * as subprocessoService from "@/services/subprocessoService";
import type {Atividade, Conhecimento, CriarAtividadeRequest, CriarConhecimentoRequest,} from "@/types/tipos";


export const useAtividadesStore = defineStore("atividades", () => {
    const atividadesPorSubprocesso = ref(new Map<number, Atividade[]>());

    const obterAtividadesPorSubprocesso = computed(
        () =>
            (codSubrocesso: number): Atividade[] => {
                return atividadesPorSubprocesso.value.get(codSubrocesso) || [];
            },
    );

    async function buscarAtividadesParaSubprocesso(codSubrocesso: number) {
        const mapa = await mapaService.obterMapaVisualizacao(codSubrocesso);
        const atividades = mapMapaVisualizacaoToAtividades(mapa);
        atividadesPorSubprocesso.value.set(codSubrocesso, atividades);
    }

    async function adicionarAtividade(
        codSubrocesso: number,
        request: CriarAtividadeRequest,
    ) {
        const novaAtividade = await atividadeService.criarAtividade(
            request,
            codSubrocesso,
        );
        const atividades =
            atividadesPorSubprocesso.value.get(codSubrocesso) || [];
        atividades.push(novaAtividade);
        atividadesPorSubprocesso.value.set(codSubrocesso, atividades);
        await buscarAtividadesParaSubprocesso(codSubrocesso);
    }

    async function removerAtividade(codSubrocesso: number, atividadeId: number) {
        await atividadeService.excluirAtividade(atividadeId);
        let atividades = atividadesPorSubprocesso.value.get(codSubrocesso) || [];
        atividades = atividades.filter((a) => a.codigo !== atividadeId);
        atividadesPorSubprocesso.value.set(codSubrocesso, atividades);
    }

    async function adicionarConhecimento(
        codSubrocesso: number,
        atividadeId: number,
        request: CriarConhecimentoRequest,
    ) {
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
    }

    async function removerConhecimento(
        codSubrocesso: number,
        atividadeId: number,
        conhecimentoId: number,
    ) {
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
    }

    async function importarAtividades(
        codSubrocessoDestino: number,
        codSubrocessoOrigem: number,
    ) {
        await subprocessoService.importarAtividades(
            codSubrocessoDestino,
            codSubrocessoOrigem,
        );
        // Recarregar as atividades do subprocesso de destino para refletir a importação
        await buscarAtividadesParaSubprocesso(codSubrocessoDestino);
    }

    async function atualizarAtividade(
        codSubrocesso: number,
        atividadeId: number,
        data: Atividade,
    ) {
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
    }

    async function atualizarConhecimento(
        codSubrocesso: number,
        atividadeId: number,
        conhecimentoId: number,
        data: Conhecimento,
    ) {
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
