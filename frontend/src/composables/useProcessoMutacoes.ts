import {ref, type Ref} from "vue";
import {useRouter} from "vue-router";
import {useToastStore} from "@/stores/toast";
import {useInvalidacaoNavegacao} from "@/composables/useInvalidacaoNavegacao";
import * as processoService from "@/services/processo";
import {aplicarSelecaoDiretaUnidadesComEquipePropria} from "@/views/processoCadastroUnidades";
import type {Processo, Unidade} from "@/types/tipos";
import {TEXTOS} from "@/constants/textos";
import {TEXTOS_SUCESSO_PROCESSO} from "@/constants/textos-processo";
import type {useProcessoForm} from "@/composables/useProcessoForm";

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

export function useProcessoMutacoes({
    formulario,
    processoEditando,
    unidadesComEquipePropriaSelecionadas,
    idsUnidadesComEquipePropriaSelecionadas,
    mostrarModalConfirmacao,
    mostrarModalRemocao,
    modalUnidadesComEquipePropriaRef,
    tratarErrosApi
}: UseProcessoMutacoesParams) {
    const router = useRouter();
    const toastStore = useToastStore();
    const {atualizarFluxoProcesso} = useInvalidacaoNavegacao();

    const isSaving = ref(false);
    const isStarting = ref(false);
    const isRemoving = ref(false);

    async function salvarProcesso() {
        formulario.limparErros();
        isSaving.value = true;
        try {
            if (processoEditando.value) {
                const request = formulario.construirAtualizarRequest(processoEditando.value.codigo);
                await processoService.atualizarProcesso(processoEditando.value.codigo, request);
                toastStore.setPending(TEXTOS_SUCESSO_PROCESSO.PROCESSO_ALTERADO);
            } else {
                const request = formulario.construirCriarRequest();
                await processoService.criarProcesso(request);
                toastStore.setPending(TEXTOS_SUCESSO_PROCESSO.PROCESSO_CRIADO);
            }
            await atualizarFluxoProcesso();
            await router.push("/painel");
            formulario.limpar();
        } catch (error) {
            tratarErrosApi(error, "Erro ao salvar processo", "Não foi possível salvar o processo.");
        } finally {
            isSaving.value = false;
        }
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
        isStarting.value = true;
        modalUnidadesComEquipePropriaRef.value?.setErro(null);
        modalUnidadesComEquipePropriaRef.value?.setProcessando(true);

        try {
            let codigoProcesso: number;
            try {
                codigoProcesso = await garantirCodigoProcessoParaInicio();
            } catch (error) {
                mostrarModalConfirmacao.value = false;
                modalUnidadesComEquipePropriaRef.value?.setProcessando(false);
                tratarErrosApi(error, "Erro ao criar processo", TEXTOS.processo.cadastro.ERRO_CRIAR_PARA_INICIAR);
                return;
            }

            if (!formulario.tipo.value) {
                throw new Error("Tipo não definido");
            }
            await processoService.iniciarProcesso(codigoProcesso, formulario.tipo.value, codigosDiretos);

            toastStore.setPending(TEXTOS_SUCESSO_PROCESSO.PROCESSO_INICIADO);
            await atualizarFluxoProcesso();
            await router.push("/painel");
            mostrarModalConfirmacao.value = false;
            modalUnidadesComEquipePropriaRef.value?.fechar();
        } catch (error) {
            mostrarModalConfirmacao.value = false;
            modalUnidadesComEquipePropriaRef.value?.fechar();
            tratarErrosApi(error, "Erro ao iniciar processo", TEXTOS.processo.cadastro.ERRO_INICIAR_PROCESSO);
        } finally {
            modalUnidadesComEquipePropriaRef.value?.setProcessando(false);
            isStarting.value = false;
        }
    }

    async function confirmarIniciarProcesso() {
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
        isRemoving.value = true;
        const descricaoRemovida = processoEditando.value.descricao;
        try {
            await processoService.excluirProcesso(processoEditando.value.codigo);
            toastStore.setPending(TEXTOS_SUCESSO_PROCESSO.PROCESSO_REMOVIDO(descricaoRemovida));
            await atualizarFluxoProcesso();
            await router.push("/painel");
            formulario.limpar();
            mostrarModalRemocao.value = false;
        } catch (error) {
            mostrarModalRemocao.value = false;
            tratarErrosApi(error, "Erro ao remover processo", TEXTOS.processo.cadastro.ERRO_REMOVER_PROCESSO);
        } finally {
            isRemoving.value = false;
        }
    }

    return {
        isSaving,
        isStarting,
        isRemoving,
        salvarProcesso,
        confirmarIniciarProcesso,
        confirmarSelecaoUnidadesComEquipePropria,
        confirmarRemocao
    };
}
