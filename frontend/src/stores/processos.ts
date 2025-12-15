import {defineStore} from "pinia";
import {computed, ref} from "vue";
import type {Page} from "@/services/painelService";
import type {
    AtualizarProcessoRequest,
    CriarProcessoRequest,
    Processo,
    ProcessoResumo,
    SubprocessoElegivel,
    TipoProcesso,
} from "@/types/tipos";
import { normalizeError, type NormalizedError } from "@/utils/apiError";
import * as painelService from "../services/painelService";
import * as processoService from "../services/processoService";

export const useProcessosStore = defineStore("processos", () => {
    const processosPainel = ref<ProcessoResumo[]>([]);
    const processosPainelPage = ref<Page<ProcessoResumo>>({} as Page<ProcessoResumo>);
    const processoDetalhe = ref<Processo | null>(null);
    const subprocessosElegiveis = ref<SubprocessoElegivel[]>([]);
    const processosFinalizados = ref<ProcessoResumo[]>([]);
    const lastError = ref<NormalizedError | null>(null);

    const obterUnidadesProcesso = computed(
        () =>
            (idProcesso: number): ProcessoResumo[] => {
                if (processoDetalhe.value && processoDetalhe.value.codigo === idProcesso) {
                    return processoDetalhe.value.resumoSubprocessos;
                }
                return [];
            },
    );

    function clearError() {
        lastError.value = null;
    }

    async function buscarProcessosPainel(
        perfil: string,
        unidade: number,
        page: number,
        size: number,
        sort?: keyof ProcessoResumo,
        order?: "asc" | "desc",
    ) {
        lastError.value = null;
        try {
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
        } catch (error) {
            lastError.value = normalizeError(error);
            throw error;
        }
    }

    async function buscarProcessosFinalizados() {
        lastError.value = null;
        try {
            processosFinalizados.value = await processoService.buscarProcessosFinalizados();
        } catch (error) {
            lastError.value = normalizeError(error);
            throw error;
        }
    }

    async function buscarProcessoDetalhe(idProcesso: number) {
        lastError.value = null;
        try {
            processoDetalhe.value = await processoService.obterDetalhesProcesso(idProcesso);
        } catch (error) {
            lastError.value = normalizeError(error);
            processoDetalhe.value = null;
            throw error;
        }
    }

    async function buscarSubprocessosElegiveis(idProcesso: number) {
        lastError.value = null;
        try {
            subprocessosElegiveis.value =
                await processoService.buscarSubprocessosElegiveis(idProcesso);
        } catch (error) {
            lastError.value = normalizeError(error);
            throw error;
        }
    }

    async function criarProcesso(payload: CriarProcessoRequest) {
        lastError.value = null;
        try {
            return await processoService.criarProcesso(payload);
        } catch (error) {
            lastError.value = normalizeError(error);
            throw error;
        }
    }

    async function atualizarProcesso(idProcesso: number, payload: AtualizarProcessoRequest) {
        lastError.value = null;
        try {
            await processoService.atualizarProcesso(idProcesso, payload);
        } catch (error) {
            lastError.value = normalizeError(error);
            throw error;
        }
    }

    async function removerProcesso(idProcesso: number) {
        lastError.value = null;
        try {
            await processoService.excluirProcesso(idProcesso);
        } catch (error) {
            lastError.value = normalizeError(error);
            throw error;
        }
    }

    async function iniciarProcesso(idProcesso: number, tipo: TipoProcesso, unidadesIds: number[]) {
        lastError.value = null;
        try {
            await processoService.iniciarProcesso(idProcesso, tipo, unidadesIds);
        } catch (error) {
            lastError.value = normalizeError(error);
            throw error;
        }
    }

    async function finalizarProcesso(idProcesso: number) {
        lastError.value = null;
        try {
            await processoService.finalizarProcesso(idProcesso);
            await buscarProcessoDetalhe(idProcesso);
        } catch (error) {
            lastError.value = normalizeError(error);
            throw error;
        }
    }

    async function processarCadastroBloco(payload: {
        codProcesso: number;
        unidades: string[];
        tipoAcao: "aceitar" | "homologar";
        unidadeUsuario: string;
    }) {
        lastError.value = null;
        try {
            await processoService.processarAcaoEmBloco(payload);
            // Após a ação em bloco, recarregar os detalhes do processo para refletir as mudanças
            await buscarProcessoDetalhe(payload.codProcesso);
        } catch (error) {
            lastError.value = normalizeError(error);
            throw error;
        }
    }

    async function alterarDataLimiteSubprocesso(id: number, dados: { novaData: string }) {
        lastError.value = null;
        try {
            await processoService.alterarDataLimiteSubprocesso(id, dados);
            if (processoDetalhe.value) {
                await buscarProcessoDetalhe(processoDetalhe.value.codigo);
            }
        } catch (error) {
            lastError.value = normalizeError(error);
            throw error;
        }
    }

    async function apresentarSugestoes(id: number, dados: { sugestoes: string }) {
        lastError.value = null;
        try {
            await processoService.apresentarSugestoes(id, dados);
            if (processoDetalhe.value) {
                await buscarProcessoDetalhe(processoDetalhe.value.codigo);
            }
        } catch (error) {
            lastError.value = normalizeError(error);
            throw error;
        }
    }

    async function validarMapa(id: number) {
        lastError.value = null;
        try {
            await processoService.validarMapa(id);
            if (processoDetalhe.value) await buscarProcessoDetalhe(processoDetalhe.value.codigo);
        } catch (error) {
            lastError.value = normalizeError(error);
            throw error;
        }
    }

    async function homologarValidacao(id: number) {
        lastError.value = null;
        try {
            await processoService.homologarValidacao(id);
            if (processoDetalhe.value) await buscarProcessoDetalhe(processoDetalhe.value.codigo);
        } catch (error) {
            lastError.value = normalizeError(error);
            throw error;
        }
    }

    async function aceitarValidacao(id: number, dados?: { observacoes?: string }) {
        lastError.value = null;
        try {
            await processoService.aceitarValidacao(id, dados);
            if (processoDetalhe.value) await buscarProcessoDetalhe(processoDetalhe.value.codigo);
        } catch (error) {
            lastError.value = normalizeError(error);
            throw error;
        }
    }

    function atualizarStatusSubprocesso(codSubprocesso: number, dados: { situacao: any, situacaoLabel: string }) {
        if (processoDetalhe.value) {
            const unidade = processoDetalhe.value.unidades.find(u => u.codSubprocesso === codSubprocesso);
            if (unidade) {
                unidade.situacaoSubprocesso = dados.situacao;
                unidade.situacaoLabel = dados.situacaoLabel;
            }
        }
    }

    return {
        processosPainel,
        processosPainelPage,
        processoDetalhe,
        subprocessosElegiveis,
        processosFinalizados,
        lastError,
        obterUnidadesDoProcesso: obterUnidadesProcesso,
        buscarProcessosPainel,
        buscarProcessosFinalizados,
        buscarProcessoDetalhe,
        buscarSubprocessosElegiveis,
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
        atualizarStatusSubprocesso,
        clearError,
    };
});
