// @sgc-auditoria ignorar: arquivoMinusculo | Composable de tela simples — funcionalidade de relatório de andamento não tem complexidade que justifique mais código
import {ref} from "vue";
import type {ProcessoResumo} from "@/types/tipos";
import * as painelService from "@/services/painelService";
import {useNotification} from "@/composables/useNotification";

export function useRelatorioAndamentoTela() {
    const {notify} = useNotification();
    const processosDisponiveis = ref<ProcessoResumo[]>([]);

    async function carregarProcessos() {
        try {
            const response = await painelService.listarProcessos({page: 0, size: 100});
            processosDisponiveis.value = response?.content ?? [];
        } catch {
            notify("Erro ao carregar processos", "danger");
        }
    }

    return {processosDisponiveis, carregarProcessos};
}
