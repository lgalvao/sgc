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
import { useErrorHandler } from "@/composables/useErrorHandler";
import { flattenTree } from "@/utils";
import * as painelService from "../services/painelService";
import * as processoService from "../services/processoService";
import * as subprocessoService from "../services/subprocessoService";

export const useProcessosStore = defineStore("processos", () => {
    const processosPainel = ref<ProcessoResumo[]>([]);
    const processosPainelPage = ref<Page<ProcessoResumo>>({} as Page<ProcessoResumo>);
    const processoDetalhe = ref<Processo | null>(null);
    const subprocessosElegiveis = ref<SubprocessoElegivel[]>([]);
    const processosFinalizados = ref<ProcessoResumo[]>([]);
    const { lastError, clearError, withErrorHandling } = useErrorHandler();

    const obterUnidadesProcesso = computed(
        () =>
            (idProcesso: number): ProcessoResumo[] => {
                if (processoDetalhe.value && processoDetalhe.value.codigo === idProcesso) {
                    return processoDetalhe.value.resumoSubprocessos;
                }
                return [];
            },
    );

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

    async function buscarContextoCompleto(idProcesso: number) {
        return withErrorHandling(async () => {
            processoDetalhe.value = null; // Limpa estado anterior
            const data = await processoService.buscarContextoCompleto(idProcesso);
            processoDetalhe.value = data.processo;
            subprocessosElegiveis.value = data.elegiveis;
        });
    }

    async function buscarProcessosFinalizados() {
        return withErrorHandling(async () => {
            processosFinalizados.value = await processoService.buscarProcessosFinalizados();
        });
    }

    async function buscarProcessoDetalhe(idProcesso: number) {
        return withErrorHandling(async () => {
            processoDetalhe.value = null; // Limpa estado anterior
            processoDetalhe.value = await processoService.obterDetalhesProcesso(idProcesso);
        }, () => {
            processoDetalhe.value = null;
        });
    }

    async function buscarSubprocessosElegiveis(idProcesso: number) {
        return withErrorHandling(async () => {
            subprocessosElegiveis.value =
                await processoService.buscarSubprocessosElegiveis(idProcesso);
        });
    }

    async function criarProcesso(payload: CriarProcessoRequest) {
        return withErrorHandling(async () => {
            return await processoService.criarProcesso(payload);
        });
    }

    async function atualizarProcesso(idProcesso: number, payload: AtualizarProcessoRequest) {
        return withErrorHandling(async () => {
            await processoService.atualizarProcesso(idProcesso, payload);
        });
    }

    async function removerProcesso(idProcesso: number) {
        return withErrorHandling(async () => {
            await processoService.excluirProcesso(idProcesso);
        });
    }

    async function iniciarProcesso(idProcesso: number, tipo: TipoProcesso, unidadesIds: number[]) {
        return withErrorHandling(async () => {
            await processoService.iniciarProcesso(idProcesso, tipo, unidadesIds);
        });
    }

    async function finalizarProcesso(idProcesso: number) {
        return withErrorHandling(async () => {
            await processoService.finalizarProcesso(idProcesso);
            await buscarProcessoDetalhe(idProcesso);
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
            // Após a ação em bloco, recarregar os detalhes do processo para refletir as mudanças
            await buscarProcessoDetalhe(payload.codProcesso);
        });
    }

    async function alterarDataLimiteSubprocesso(id: number, dados: { novaData: string }) {
        return withErrorHandling(async () => {
            await processoService.alterarDataLimiteSubprocesso(id, dados);
            if (processoDetalhe.value) {
                await buscarProcessoDetalhe(processoDetalhe.value.codigo);
            }
        });
    }

    async function apresentarSugestoes(id: number, dados: { sugestoes: string }) {
        return withErrorHandling(async () => {
            await processoService.apresentarSugestoes(id, dados);
            if (processoDetalhe.value) {
                await buscarProcessoDetalhe(processoDetalhe.value.codigo);
            }
        });
    }

    async function validarMapa(id: number) {
        return withErrorHandling(async () => {
            await processoService.validarMapa(id);
            if (processoDetalhe.value) await buscarProcessoDetalhe(processoDetalhe.value.codigo);
        });
    }

    async function homologarValidacao(id: number) {
        return withErrorHandling(async () => {
            await processoService.homologarValidacao(id);
            if (processoDetalhe.value) await buscarProcessoDetalhe(processoDetalhe.value.codigo);
        });
    }

    async function aceitarValidacao(id: number, dados?: { observacoes?: string }) {
        return withErrorHandling(async () => {
            await processoService.aceitarValidacao(id, dados);
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

            const all = flattenTree(processoDetalhe.value.unidades || [], 'filhos');

            const unidadeExemplo = all.find((u) => u.codUnidade === ids[0]);

            if (!unidadeExemplo) {
                throw new Error("Unidade selecionada não encontrada no contexto do processo.");
            }

            const codSubprocessoBase = unidadeExemplo.codSubprocesso;
            const situacao = unidadeExemplo.situacaoSubprocesso;

            const payload = {
                unidadeCodigos: ids,
                dataLimite: dataLimite,
            };

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
        });
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
