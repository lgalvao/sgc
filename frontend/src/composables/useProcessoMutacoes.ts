import type {Ref} from "vue";
import {useRouter} from "vue-router";
import {useToast} from "@/composables/useToast";
import {useInvalidacaoNavegacao} from "@/composables/useInvalidacaoNavegacao";
import * as processoService from "@/services/processo";
import {aplicarSelecaoDiretaUnidadesComEquipePropria} from "@/views/processoCadastroUnidades";
import type {Processo, Unidade} from "@/types/tipos";
import {TEXTOS} from "@/constants/textos";
import {TEXTOS_SUCESSO_PROCESSO} from "@/constants/textos-processo";
import type {useProcessoForm} from "@/composables/useProcessoForm";
import {useAsyncAction} from "@/composables/useAsyncAction";

export interface ModalUnidadesComEquipePropriaRef {
    setErro: (erro: string | null) => void;
    setProcessando: (processando: boolean) => void;
    fechar: () => void;
    abrir: () => void;
}

interface UseProcessoMutacoesParams {
    formulario: ReturnType<typeof useProcessoForm>;
    processoEditando: Ref<Processo | null>;
    unidadesComEquipePropriaSelecionadas: Ref<Unidade[]>;
    idsUnidadesComEquipePropriaSelecionadas: Ref<number[]>;
    mostrarModalConfirmacao: Ref<boolean>;
    mostrarModalRemocao: Ref<boolean>;
    modalUnidadesComEquipePropriaRef: Ref<ModalUnidadesComEquipePropriaRef | null>;
    tratarErrosApi: (error: unknown, titulo: string, mensagemPadrao: string) => void;
}

function tratarTipoAusenteNaInicializacao(params: {
    mostrarModalConfirmacao: Ref<boolean>;
    modalUnidadesComEquipePropriaRef: Ref<ModalUnidadesComEquipePropriaRef | null>;
    tratarErrosApi: (error: unknown, titulo: string, mensagemPadrao: string) => void;
}) {
    params.mostrarModalConfirmacao.value = false;
    params.modalUnidadesComEquipePropriaRef.value?.fechar();
    params.tratarErrosApi(
        new Error("Tipo de processo não informado no formulário."),
        "Erro ao criar processo",
        TEXTOS.processo.cadastro.ERRO_CRIAR_PARA_INICIAR,
    );
}

export function useProcessoMutacoes({formulario, processoEditando, unidadesComEquipePropriaSelecionadas, idsUnidadesComEquipePropriaSelecionadas, mostrarModalConfirmacao, mostrarModalRemocao, modalUnidadesComEquipePropriaRef, tratarErrosApi}: UseProcessoMutacoesParams) {
    const router = useRouter();
    const {registrarPendente} = useToast();
    const {atualizarFluxoProcesso} = useInvalidacaoNavegacao();
    const acaoSalvar = useAsyncAction();
    const acaoIniciar = useAsyncAction();
    const acaoRemover = useAsyncAction();
    async function salvarProcesso() {
        formulario.limparErros();
        await acaoSalvar.executar(
            async () => {
                if (processoEditando.value) {
                    const request = formulario.construirAtualizarRequest(processoEditando.value.codigo);
                    await processoService.atualizarProcesso(processoEditando.value.codigo, request);
                    registrarPendente(TEXTOS_SUCESSO_PROCESSO.PROCESSO_ALTERADO);
                    return;
                }
                const request = formulario.construirCriarRequest();
                await processoService.criarProcesso(request);
                registrarPendente(TEXTOS_SUCESSO_PROCESSO.PROCESSO_CRIADO);
            },
            "Não foi possível salvar o processo.",
            {
                relancarErro: false,
                aoSucesso: async () => {
                    await atualizarFluxoProcesso();
                    await router.push("/painel");
                    formulario.limpar();
                },
                aoOcorrerErro: (_erro, causa) => {
                    tratarErrosApi(causa, "Erro ao salvar processo", "Não foi possível salvar o processo.");
                },
            },
        );
    }
    async function garantirCodigoProcessoParaInicio() {
        if (processoEditando.value?.codigo) {
            return processoEditando.value.codigo;
        }
        const request = formulario.construirCriarRequest();
        const novoProcesso = await processoService.criarProcesso(request);
        return novoProcesso.codigo;
    }
    async function iniciarProcessoComSelecaoDireta(codigosDiretos: number[]) {
        formulario.limparErros();
        modalUnidadesComEquipePropriaRef.value?.setErro(null);
        modalUnidadesComEquipePropriaRef.value?.setProcessando(true);
        try {
            await acaoIniciar.executar(
                async () => {
                    const codigoProcesso = await garantirCodigoProcessoParaInicio();
                    await processoService.iniciarProcesso(codigoProcesso, formulario.tipo.value!, codigosDiretos);
                    registrarPendente(TEXTOS_SUCESSO_PROCESSO.PROCESSO_INICIADO);
                },
                TEXTOS.processo.cadastro.ERRO_INICIAR_PROCESSO,
                {
                    relancarErro: false,
                    aoSucesso: async () => {
                        await atualizarFluxoProcesso();
                        await router.push("/painel");
                        mostrarModalConfirmacao.value = false;
                        modalUnidadesComEquipePropriaRef.value?.fechar();
                    },
                    aoOcorrerErro: (_erro, causa) => {
                        mostrarModalConfirmacao.value = false;
                        modalUnidadesComEquipePropriaRef.value?.fechar();
                        tratarErrosApi(causa, "Erro ao iniciar processo", TEXTOS.processo.cadastro.ERRO_INICIAR_PROCESSO);
                    },
                },
            );
        } finally {
            modalUnidadesComEquipePropriaRef.value?.setProcessando(false);
        }
    }
    async function confirmarIniciarProcesso() {
        if (!formulario.tipo.value) {
            tratarTipoAusenteNaInicializacao({
                mostrarModalConfirmacao,
                modalUnidadesComEquipePropriaRef,
                tratarErrosApi,
            });
            return;
        }
        if (unidadesComEquipePropriaSelecionadas.value.length > 0) {
            mostrarModalConfirmacao.value = false;
            modalUnidadesComEquipePropriaRef.value?.abrir();
            return;
        }
        await iniciarProcessoComSelecaoDireta(formulario.unidadesSelecionadas.value);
    }
    async function confirmarSelecaoUnidadesComEquipePropria(dados: { ids: number[] }) {
        const codigosDiretos = aplicarSelecaoDiretaUnidadesComEquipePropria(
            formulario.unidadesSelecionadas.value,
            idsUnidadesComEquipePropriaSelecionadas.value,
            dados.ids,
        );
        await iniciarProcessoComSelecaoDireta(codigosDiretos);
    }
    async function confirmarRemocao() {
        if (!processoEditando.value) {
            mostrarModalRemocao.value = false;
            return;
        }
        const descricaoRemovida = processoEditando.value.descricao;
        await acaoRemover.executar(
            () => processoService.excluirProcesso(processoEditando.value!.codigo),
            TEXTOS.processo.cadastro.ERRO_REMOVER_PROCESSO,
            {
                relancarErro: false,
                aoSucesso: async () => {
                    registrarPendente(TEXTOS_SUCESSO_PROCESSO.PROCESSO_REMOVIDO(descricaoRemovida));
                    await atualizarFluxoProcesso();
                    await router.push("/painel");
                    formulario.limpar();
                    mostrarModalRemocao.value = false;
                },
                aoOcorrerErro: (_erro, causa) => {
                    mostrarModalRemocao.value = false;
                    tratarErrosApi(causa, "Erro ao remover processo", TEXTOS.processo.cadastro.ERRO_REMOVER_PROCESSO);
                },
            },
        );
    }
    return {
        isSaving: acaoSalvar.carregando,
        isStarting: acaoIniciar.carregando,
        isRemoving: acaoRemover.carregando,
        salvarProcesso,
        confirmarIniciarProcesso,
        confirmarSelecaoUnidadesComEquipePropria,
        confirmarRemocao,
    };
}
