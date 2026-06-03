import {ref, watch, onMounted, nextTick, type Ref} from "vue";
import {useRoute, useRouter} from "vue-router";
import {isErroCanceladoHttp} from "@/axios-setup";
import {useArvoreElegibilidadeQuery} from "@/composables/useUnidadeQuery";
import * as processoService from "@/services/processo";
import {removerUnidadesSemEquipe, filtrarSelecionadasPorElegibilidade} from "@/views/processoCadastroUnidades";
import type {Processo, Unidade, TipoProcesso} from "@/types/tipos";
import {SituacaoProcesso} from "@/types/tipos";
import {TEXTOS} from "@/constants/textos";
import {logger} from "@/utils";
import type {useProcessoForm} from "@/composables/useProcessoForm";
import type {VarianteAlerta} from "@/composables/useNotification";

export interface FormFieldsRef {
    focarDescricao?: () => void;
    focarPrimeiroErro?: () => void;
}

interface UseProcessoCargaParams {
    formulario: ReturnType<typeof useProcessoForm>;
    formFieldsRef: Ref<FormFieldsRef | null>;
    unidades: Ref<Unidade[]>;
    isLoadingUnidades: Ref<boolean>;
    isLoadingData: Ref<boolean>;
    processoEditando: Ref<Processo | null>;
    notify: (mensagem: string, variante?: VarianteAlerta) => void;
}

export function useProcessoCadastroCarga({
    formulario,
    formFieldsRef,
    unidades,
    isLoadingUnidades,
    isLoadingData,
    processoEditando,
    notify
}: UseProcessoCargaParams) {
    const route = useRoute();
    const router = useRouter();
    const tipoRef = ref<string | null>(null);
    const codigoRef = ref<number | undefined>(undefined);
    const query = useArvoreElegibilidadeQuery(tipoRef, codigoRef);
    const carregamentoInicialConcluido = ref(false);
    const inicializando = ref(false);

    function sincronizarUnidadesSelecionadasElegiveis(unidadesArvore: Unidade[]) {
        const selecionadasFiltradas = filtrarSelecionadasPorElegibilidade(
            formulario.unidadesSelecionadas.value,
            unidadesArvore,
        );
        if (selecionadasFiltradas.length !== formulario.unidadesSelecionadas.value.length) {
            formulario.unidadesSelecionadas.value = selecionadasFiltradas;
        }
    }

    async function buscarUnidadesParaProcesso(tipoProcesso: TipoProcesso, codigoProcesso?: number) {
        isLoadingUnidades.value = true;
        tipoRef.value = tipoProcesso;
        codigoRef.value = codigoProcesso;
        try {
            const resultado = await query.refetch();
            const unidadesMapeadas = resultado.data ?? [];
            const unidadesSemSemEquipe = removerUnidadesSemEquipe(unidadesMapeadas);
            unidades.value = unidadesSemSemEquipe;
            sincronizarUnidadesSelecionadasElegiveis(unidadesSemSemEquipe);
        } catch (error: unknown) {
            if (isErroCanceladoHttp(error)) return;
            logger.error("Erro ao buscar unidades:", error);
            notify(TEXTOS.processo.cadastro.ERRO_CARREGAR_UNIDADES, "danger");
        } finally {
            isLoadingUnidades.value = false;
        }
    }

    async function carregarProcessoParaEdicao(codigoProcesso: number) {
        isLoadingData.value = true;
        inicializando.value = true;
        try {
            const processo = await processoService.obterDetalhesProcesso(codigoProcesso);
            if (processo.situacao !== SituacaoProcesso.CRIADO) {
                await router.push(`/processo/${processo.codigo}`);
                return;
            }

            processoEditando.value = processo;
            formulario.descricao.value = processo.descricao;
            formulario.tipo.value = processo.tipo;
            formulario.dataLimite.value = processo.dataLimite.split("T")[0];
            formulario.unidadesSelecionadas.value = processo.unidades.map((unidade) => unidade.codUnidade);
            await buscarUnidadesParaProcesso(processo.tipo, processo.codigo);
            await nextTick();
        } catch (error) {
            if (isErroCanceladoHttp(error)) return;
            notify(TEXTOS.processo.cadastro.ERRO_CARREGAR_DETALHES, "danger");
            logger.error("Erro ao carregar processo:", error);
        } finally {
            isLoadingData.value = false;
            inicializando.value = false;
        }
    }

    onMounted(async () => {
        const codigoProcesso = route.query.codProcesso;
        if (codigoProcesso) {
            await carregarProcessoParaEdicao(Number(codigoProcesso));
        } else if (formulario.tipo.value) {
            await buscarUnidadesParaProcesso(formulario.tipo.value);
        }

        if (!processoEditando.value) {
            await nextTick();
            formFieldsRef.value?.focarDescricao?.();
        }
        carregamentoInicialConcluido.value = true;
    });

    watch(formulario.tipo, async (novoTipo) => {
        if (inicializando.value) return;
        const codigoProcesso = processoEditando.value ? processoEditando.value.codigo : undefined;
        if (novoTipo) {
            await buscarUnidadesParaProcesso(novoTipo, codigoProcesso);
        }
    });

    return {
        carregamentoInicialConcluido,
        buscarUnidadesParaProcesso
    };
}
