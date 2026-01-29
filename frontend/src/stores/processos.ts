import { defineStore, storeToRefs } from "pinia";
import { computed } from "vue";
import { useProcessosCoreStore } from "./processos/core";
import { useProcessosWorkflowStore } from "./processos/workflow";
import { useProcessosContextStore } from "./processos/context";

// Re-export new stores for direct usage
export * from "./processos/core";
export * from "./processos/workflow";
export * from "./processos/context";

export const useProcessosStore = defineStore("processos", () => {
    const core = useProcessosCoreStore();
    const workflow = useProcessosWorkflowStore();
    const context = useProcessosContextStore();

    // Extract Refs to maintain reactivity
    const { 
        processosPainel, 
        processosPainelPage, 
        processoDetalhe, 
        processosFinalizados 
    } = storeToRefs(core);

    const { 
        subprocessosElegiveis 
        // obterUnidadesDoProcesso is a computed, so it comes via storeToRefs? 
        // Computeds are refs in Pinia setup stores. Yes.
    } = storeToRefs(context);

    // obterUnidadesDoProcesso in context.ts returns a function (computed returning function).
    // storeToRefs handles it.
    
    // Aggregated Error Handling
    const lastError = computed(() => core.lastError || workflow.lastError || context.lastError);
    function clearError() {
        core.clearError();
        workflow.clearError();
        context.clearError();
    }

    return {
        // Core State
        processosPainel,
        processosPainelPage,
        processoDetalhe,
        processosFinalizados,
        
        // Core Actions
        buscarProcessosPainel: core.buscarProcessosPainel,
        buscarProcessosFinalizados: core.buscarProcessosFinalizados,
        buscarProcessoDetalhe: core.buscarProcessoDetalhe,
        criarProcesso: core.criarProcesso,
        atualizarProcesso: core.atualizarProcesso,
        removerProcesso: core.removerProcesso,

        // Workflow Actions
        iniciarProcesso: workflow.iniciarProcesso,
        finalizarProcesso: workflow.finalizarProcesso,
        processarCadastroBloco: workflow.processarCadastroBloco,
        alterarDataLimiteSubprocesso: workflow.alterarDataLimiteSubprocesso,
        apresentarSugestoes: workflow.apresentarSugestoes,
        validarMapa: workflow.validarMapa,
        homologarValidacao: workflow.homologarValidacao,
        aceitarValidacao: workflow.aceitarValidacao,
        executarAcaoBloco: workflow.executarAcaoBloco,

        // Context State & Actions
        subprocessosElegiveis,
        obterUnidadesDoProcesso: context.obterUnidadesDoProcesso,
        buscarContextoCompleto: context.buscarContextoCompleto,
        buscarSubprocessosElegiveis: context.buscarSubprocessosElegiveis,

        // Common
        lastError,
        clearError,
    };
});
