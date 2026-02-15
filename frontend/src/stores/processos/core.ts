import {defineStore} from "pinia";
import {ref} from "vue";
import type {Page} from "@/services/painelService";
import * as painelService from "@/services/painelService";
import type {AtualizarProcessoRequest, CriarProcessoRequest, Processo, ProcessoResumo,} from "@/types/tipos";
import {useErrorHandler} from "@/composables/useErrorHandler";
import * as processoService from "@/services/processoService";

export const useProcessosCoreStore = defineStore("processos-core", () => {
    const processosPainel = ref<ProcessoResumo[]>([]);
    const processosPainelPage = ref<Page<ProcessoResumo>>({} as Page<ProcessoResumo>);
    const processoDetalhe = ref<Processo | null>(null);
    const processosFinalizados = ref<ProcessoResumo[]>([]);
    
    const { lastError, clearError, withErrorHandling } = useErrorHandler();

    async function buscarProcessosPainel(
        perfil: string,
        unidade: number,
        page: number,
        size: number,
        sort?: keyof ProcessoResumo,
        order?: "asc" | "desc",
    ) {
        return withErrorHandling(async () => {
            const response = await painelService.listarProcessos(
                perfil,
                unidade,
                page,
                size,
                sort,
                order,
            );
            processosPainel.value = response.content;
            processosPainelPage.value = response;
        });
    }

    async function buscarProcessosFinalizados() {
        return withErrorHandling(async () => {
            processosFinalizados.value = await processoService.buscarProcessosFinalizados();
        });
    }

    async function buscarProcessoDetalhe(codigoProcesso: number) {
        return withErrorHandling(async () => {
            processoDetalhe.value = null; // Limpa estado anterior
            processoDetalhe.value = await processoService.obterDetalhesProcesso(codigoProcesso);
        }, () => {
            processoDetalhe.value = null;
        });
    }

    async function criarProcesso(payload: CriarProcessoRequest) {
        return withErrorHandling(async () => {
            return await processoService.criarProcesso(payload);
        });
    }

    async function atualizarProcesso(codigoProcesso: number, payload: AtualizarProcessoRequest) {
        return withErrorHandling(async () => {
            await processoService.atualizarProcesso(codigoProcesso, payload);
        });
    }

    async function removerProcesso(codigoProcesso: number) {
        return withErrorHandling(async () => {
            await processoService.excluirProcesso(codigoProcesso);
        });
    }
    
    // Setter action to allow other stores to update details
    function setProcessoDetalhe(processo: Processo | null) {
        processoDetalhe.value = processo;
    }

    return {
        processosPainel,
        processosPainelPage,
        processoDetalhe,
        processosFinalizados,
        lastError,
        buscarProcessosPainel,
        buscarProcessosFinalizados,
        buscarProcessoDetalhe,
        criarProcesso,
        atualizarProcesso,
        removerProcesso,
        setProcessoDetalhe,
        clearError,
    };
});
