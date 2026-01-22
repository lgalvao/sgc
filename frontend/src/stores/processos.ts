import { defineStore } from "pinia";
import { computed, ref } from "vue";
import type { Page } from "@/services/painelService";
import type {
    AtualizarProcessoRequest,
    CriarProcessoRequest,
    Processo,
    ProcessoResumo,
    SubprocessoElegivel,
    TipoProcesso,
} from "@/types/tipos";
import { SituacaoSubprocesso } from "@/types/tipos";
import { type NormalizedError, normalizeError } from "@/utils/apiError";
import * as painelService from "../services/painelService";
import * as processoService from "../services/processoService";
import * as subprocessoService from "../services/subprocessoService";

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

    async function buscarContextoCompleto(idProcesso: number) {
        lastError.value = null;
        processoDetalhe.value = null; // Limpa estado anterior
        try {
            const data = await processoService.buscarContextoCompleto(idProcesso);
            processoDetalhe.value = data.processo;
            subprocessosElegiveis.value = data.elegiveis;
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
        processoDetalhe.value = null; // Limpa estado anterior
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

    async function executarAcaoBloco(
        acao: 'aceitar' | 'homologar' | 'disponibilizar',
        ids: number[],
        dataLimite?: string
    ) {
        lastError.value = null;
        if (!processoDetalhe.value) {
            const err = new Error("Detalhes do processo não carregados.");
            lastError.value = normalizeError(err);
            throw err;
        }

        const flatten = (nodes: any[]): any[] => {
            let res: any[] = [];
            for (const node of nodes) {
                res.push(node);
                if (node.filhos) res = res.concat(flatten(node.filhos));
            }
            return res;
        };
        const all = flatten(processoDetalhe.value.unidades || []);

        const unidadeExemplo = all.find((u) => u.codUnidade === ids[0]);

        if (!unidadeExemplo) {
            const err = new Error("Unidade selecionada não encontrada no contexto do processo.");
            lastError.value = normalizeError(err);
            throw err;
        }

        const codSubprocessoBase = unidadeExemplo.codSubprocesso;
        const situacao = unidadeExemplo.situacaoSubprocesso;

        const payload = {
            unidadeCodigos: ids,
            dataLimite: dataLimite,
        };

        try {
            if (acao === 'aceitar') {
                if (
                    situacao === SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO ||
                    situacao === SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA
                ) {
                    await subprocessoService.aceitarCadastroEmBloco(codSubprocessoBase, payload);
                } else if (
                    situacao === SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO ||
                    situacao === SituacaoSubprocesso.REVISAO_MAPA_VALIDADO
                ) {
                    await subprocessoService.aceitarValidacaoEmBloco(codSubprocessoBase, payload);
                } else {
                    throw new Error(`Situação ${situacao} não permite ação de aceitar em bloco.`);
                }
            } else if (acao === 'homologar') {
                if (
                    situacao === SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO ||
                    situacao === SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA
                ) {
                    await subprocessoService.homologarCadastroEmBloco(codSubprocessoBase, payload);
                } else if (
                    situacao === SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO ||
                    situacao === SituacaoSubprocesso.REVISAO_MAPA_VALIDADO
                ) {
                    await subprocessoService.homologarValidacaoEmBloco(codSubprocessoBase, payload);
                } else {
                    throw new Error(`Situação ${situacao} não permite ação de homologar em bloco.`);
                }
            } else if (acao === 'disponibilizar') {
                await subprocessoService.disponibilizarMapaEmBloco(codSubprocessoBase, payload);
            }

            // Reload details
            await buscarProcessoDetalhe(processoDetalhe.value.codigo);
        } catch (error) {
            lastError.value = normalizeError(error);
            throw error;
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
        buscarContextoCompleto,
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
        executarAcaoBloco,
        clearError,
    };
});
