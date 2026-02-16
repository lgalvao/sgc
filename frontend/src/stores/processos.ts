import {defineStore} from "pinia";
import {ref} from "vue";
import type {Page} from "@/services/painelService";
import * as painelService from "@/services/painelService";
import type {
    AtualizarProcessoRequest,
    CriarProcessoRequest,
    Processo,
    ProcessoResumo,
    SubprocessoElegivel,
    TipoProcesso,
} from "@/types/tipos";
import {useErrorHandler} from "@/composables/useErrorHandler";
import * as processoService from "@/services/processoService";
import {useFeedbackStore} from "@/stores/feedback";

/**
 * Store consolidado de processos
 * Consolida core, workflow e context em um único arquivo para simplificar navegação.
 */
export const useProcessosStore = defineStore("processos", () => {
    // ======================
    // ESTADO (Core)
    // ======================
    const processosPainel = ref<ProcessoResumo[]>([]);
    const processosPainelPage = ref<Page<ProcessoResumo>>({} as Page<ProcessoResumo>);
    const processoDetalhe = ref<Processo | null>(null);
    const processosFinalizados = ref<ProcessoResumo[]>([]);
    
    // ======================
    // ESTADO (Context)
    // ======================
    const subprocessosElegiveis = ref<SubprocessoElegivel[]>([]);
    
    // ======================
    // ERROR HANDLING
    // ======================
    const { lastError, clearError, withErrorHandling } = useErrorHandler();

    // ======================
    // AÇÕES CORE
    // ======================
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
    
    function setProcessoDetalhe(processo: Processo | null) {
        processoDetalhe.value = processo;
    }

    // ======================
    // AÇÕES WORKFLOW
    // ======================
    const feedbackStore = useFeedbackStore();

    async function iniciarProcesso(codigoProcesso: number, tipo: TipoProcesso, unidadesIds: number[]) {
        return withErrorHandling(async () => {
            await processoService.iniciarProcesso(codigoProcesso, tipo, unidadesIds);
        });
    }

    async function finalizarProcesso(codigoProcesso: number) {
        return withErrorHandling(async () => {
            await processoService.finalizarProcesso(codigoProcesso);
            await buscarProcessoDetalhe(codigoProcesso);
        });
    }

    async function processarCadastroBloco(payload: {
        codProcesso: number;
        unidades: string[];
        tipoAcao: "aceitar" | "homologar";
        unidadeUsuario: string;
    }) {
        return withErrorHandling(async () => {
            await processoService.processarAcaoEmBloco(payload);
            await buscarProcessoDetalhe(payload.codProcesso);
        });
    }

    async function alterarDataLimiteSubprocesso(codigo: number, dados: { novaData: string }) {
        return withErrorHandling(async () => {
            await processoService.alterarDataLimiteSubprocesso(codigo, dados);
            if (processoDetalhe.value) {
                await buscarProcessoDetalhe(processoDetalhe.value.codigo);
            }
        });
    }

    async function apresentarSugestoes(codigo: number, dados: { sugestoes: string }) {
        return withErrorHandling(async () => {
            await processoService.apresentarSugestoes(codigo, dados);
            if (processoDetalhe.value) {
                await buscarProcessoDetalhe(processoDetalhe.value.codigo);
            }
        });
    }

    async function validarMapa(codigo: number) {
        return withErrorHandling(async () => {
            await processoService.validarMapa(codigo);
            if (processoDetalhe.value) await buscarProcessoDetalhe(processoDetalhe.value.codigo);
        });
    }

    async function homologarValidacao(codigo: number) {
        return withErrorHandling(async () => {
            await processoService.homologarValidacao(codigo);
            if (processoDetalhe.value) await buscarProcessoDetalhe(processoDetalhe.value.codigo);
        });
    }

    async function aceitarValidacao(codigo: number, dados?: { observacoes?: string }) {
        return withErrorHandling(async () => {
            await processoService.aceitarValidacao(codigo, dados);
            if (processoDetalhe.value) await buscarProcessoDetalhe(processoDetalhe.value.codigo);
        });
    }

    async function executarAcaoBloco(
        acao: 'aceitar' | 'homologar' | 'disponibilizar',
        ids: number[],
        dataLimite?: string
    ) {
        return withErrorHandling(async () => {
            if (!processoDetalhe.value) {
                throw new Error("Detalhes do processo não carregados.");
            }
            const payload = {
                unidadeCodigos: ids,
                acao,
                dataLimite: dataLimite,
            };
            await processoService.executarAcaoEmBloco(processoDetalhe.value.codigo, payload);
            await buscarProcessoDetalhe(processoDetalhe.value.codigo);
        });
    }

    async function enviarLembrete(codProcesso: number, unidadeCodigo: number) {
        return withErrorHandling(async () => {
            await processoService.enviarLembrete(codProcesso, unidadeCodigo);
            feedbackStore.show("Lembrete enviado", "Lembrete de prazo enviado com sucesso.", "success");
        });
    }

    // ======================
    // AÇÕES CONTEXT
    // ======================
    function obterUnidadesDoProcesso(codigoProcesso: number): ProcessoResumo[] {
        if (processoDetalhe.value && processoDetalhe.value.codigo === codigoProcesso) {
            return processoDetalhe.value.resumoSubprocessos;
        }
        return [];
    }

    async function buscarContextoCompleto(codigoProcesso: number) {
        return withErrorHandling(async () => {
            setProcessoDetalhe(null);
            const data = await processoService.buscarContextoCompleto(codigoProcesso);
            setProcessoDetalhe(data);
            subprocessosElegiveis.value = data.elegiveis;
        });
    }

    async function buscarSubprocessosElegiveis(codigoProcesso: number) {
        return withErrorHandling(async () => {
            subprocessosElegiveis.value =
                await processoService.buscarSubprocessosElegiveis(codigoProcesso);
        });
    }

    // ======================
    // RETORNO
    // ======================
    return {
        // Estado Core
        processosPainel,
        processosPainelPage,
        processoDetalhe,
        processosFinalizados,
        
        // Ações Core
        buscarProcessosPainel,
        buscarProcessosFinalizados,
        buscarProcessoDetalhe,
        criarProcesso,
        atualizarProcesso,
        removerProcesso,

        // Ações Workflow
        iniciarProcesso,
        finalizarProcesso,
        processarCadastroBloco,
        alterarDataLimiteSubprocesso,
        apresentarSugestoes,
        validarMapa,
        homologarValidacao,
        aceitarValidacao,
        executarAcaoBloco,
        enviarLembrete,

        // Estado e Ações Context
        subprocessosElegiveis,
        obterUnidadesDoProcesso,
        buscarContextoCompleto,
        buscarSubprocessosElegiveis,

        // Erro
        lastError,
        clearError,
    };
});
