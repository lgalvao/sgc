import {defineStore} from "pinia";
import type {TipoProcesso,} from "@/types/tipos";
import {SituacaoSubprocesso} from "@/types/tipos";
import {useErrorHandler} from "@/composables/useErrorHandler";
import {flattenTree} from "@/utils";
import * as processoService from "@/services/processoService";
import * as subprocessoService from "@/services/subprocessoService";
import {useProcessosCoreStore} from "./core";
import {useFeedbackStore} from "@/stores/feedback";

export const useProcessosWorkflowStore = defineStore("processos-workflow", () => {
    const { lastError, clearError, withErrorHandling } = useErrorHandler();
    const coreStore = useProcessosCoreStore();
    const feedbackStore = useFeedbackStore();

    async function iniciarProcesso(codigoProcesso: number, tipo: TipoProcesso, unidadesIds: number[]) {
        return withErrorHandling(async () => {
            await processoService.iniciarProcesso(codigoProcesso, tipo, unidadesIds);
        });
    }

    async function finalizarProcesso(codigoProcesso: number) {
        return withErrorHandling(async () => {
            await processoService.finalizarProcesso(codigoProcesso);
            await coreStore.buscarProcessoDetalhe(codigoProcesso);
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
            await coreStore.buscarProcessoDetalhe(payload.codProcesso);
        });
    }

    async function alterarDataLimiteSubprocesso(codigo: number, dados: { novaData: string }) {
        return withErrorHandling(async () => {
            await processoService.alterarDataLimiteSubprocesso(codigo, dados);
            if (coreStore.processoDetalhe) {
                await coreStore.buscarProcessoDetalhe(coreStore.processoDetalhe.codigo);
            }
        });
    }

    async function apresentarSugestoes(codigo: number, dados: { sugestoes: string }) {
        return withErrorHandling(async () => {
            await processoService.apresentarSugestoes(codigo, dados);
            if (coreStore.processoDetalhe) {
                await coreStore.buscarProcessoDetalhe(coreStore.processoDetalhe.codigo);
            }
        });
    }

    async function validarMapa(codigo: number) {
        return withErrorHandling(async () => {
            await processoService.validarMapa(codigo);
            if (coreStore.processoDetalhe) await coreStore.buscarProcessoDetalhe(coreStore.processoDetalhe.codigo);
        });
    }

    async function homologarValidacao(codigo: number) {
        return withErrorHandling(async () => {
            await processoService.homologarValidacao(codigo);
            if (coreStore.processoDetalhe) await coreStore.buscarProcessoDetalhe(coreStore.processoDetalhe.codigo);
        });
    }

    async function aceitarValidacao(codigo: number, dados?: { observacoes?: string }) {
        return withErrorHandling(async () => {
            await processoService.aceitarValidacao(codigo, dados);
            if (coreStore.processoDetalhe) await coreStore.buscarProcessoDetalhe(coreStore.processoDetalhe.codigo);
        });
    }

    async function executarAcaoBloco(
        acao: 'aceitar' | 'homologar' | 'disponibilizar',
        ids: number[],
        dataLimite?: string
    ) {
        return withErrorHandling(async () => {
            if (!coreStore.processoDetalhe) {
                throw new Error("Detalhes do processo não carregados.");
            }

            const all = flattenTree(coreStore.processoDetalhe.unidades || [], 'filhos');

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
            await coreStore.buscarProcessoDetalhe(coreStore.processoDetalhe.codigo);
        });
    }

    async function enviarLembrete(codProcesso: number, unidadeCodigo: number) {
        return withErrorHandling(async () => {
            await processoService.enviarLembrete(codProcesso, unidadeCodigo);
            feedbackStore.show("Lembrete enviado", "Lembrete de prazo enviado com sucesso.", "success");
        });
    }

    return {
        lastError,
        clearError,
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
    };
});
