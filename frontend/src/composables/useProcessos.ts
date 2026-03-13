import {storeToRefs} from "pinia";
import {useProcessosStore} from "@/stores/processos";

/**
 * Composable para gestão de processos.
 * Atua como um wrapper para a useProcessosStore seguindo o plano de simplificação,
 * garantindo compatibilidade com o estado global e infraestrutura de testes.
 */
export function useProcessos() {
    const store = useProcessosStore();
    
    // Estados reativos da store
    const {
        processosPainel,
        processosPainelPage,
        processoDetalhe,
        processosFinalizados,
        processosParaImportacao,
        subprocessosElegiveis,
        carregando,
        lastError
    } = storeToRefs(store);
    
    // Ações da store (expostas diretamente para permitir espionagem em testes)
    const {
        clearError,
        buscarProcessosPainel,
        buscarProcessosFinalizados,
        buscarProcessosParaImportacao,
        buscarUnidadesParaImportacao,
        buscarProcessoDetalhe,
        criarProcesso,
        atualizarProcesso,
        removerProcesso,
        iniciarProcesso,
        finalizarProcesso,
        processarCadastroBloco,
        alterarDataLimiteSubprocesso,
        apresentarSugestoes,
        validarMapa,
        homologarValidacao,
        aceitarValidacao,
        devolverValidacao,
        executarAcaoBloco,
        enviarLembrete,
        buscarContextoCompleto,
        buscarSubprocessosElegiveis,
        withErrorHandling
    } = store;

    return {
        // Estado
        carregando,
        processosPainel,
        processosPainelPage,
        processoDetalhe,
        processosFinalizados,
        processosParaImportacao,
        subprocessosElegiveis,
        lastError,
        
        // Ações
        clearError,
        buscarProcessosPainel,
        buscarProcessosFinalizados,
        buscarProcessosParaImportacao,
        buscarUnidadesParaImportacao,
        buscarProcessoDetalhe,
        criarProcesso,
        atualizarProcesso,
        removerProcesso,
        iniciarProcesso,
        finalizarProcesso,
        processarCadastroBloco,
        alterarDataLimiteSubprocesso,
        apresentarSugestoes,
        validarMapa,
        homologarValidacao,
        aceitarValidacao,
        devolverValidacao,
        executarAcaoBloco,
        enviarLembrete,
        buscarContextoCompleto,
        buscarSubprocessosElegiveis,
        withErrorHandling
    };
}
