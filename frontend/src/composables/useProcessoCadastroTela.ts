import {computed, nextTick, onMounted, ref, watch, type Ref} from "vue";
import {useRoute, useRouter} from "vue-router";
import {isErroCanceladoHttp} from "@/axios-setup";
import {useDiagnosticoOrganizacionalAlert} from "@/composables/useDiagnosticoOrganizacionalAlert";
import {useInvalidacaoNavegacao} from "@/composables/useInvalidacaoNavegacao";
import {useNotification} from "@/composables/useNotification";
import type {VarianteAlerta} from "@/composables/useNotification";
import {usePerfil} from "@/composables/usePerfil";
import {useProcessoForm} from "@/composables/useProcessoForm";
import {useValidacaoFormulario} from "@/composables/useValidacaoFormulario";
import {TEXTOS} from "@/constants/textos";
import {TEXTOS_SUCESSO_PROCESSO} from "@/constants/textos-processo";
import * as processoService from "@/services/processo";
import {useToastStore} from "@/stores/toast";
import {useUnidadeStore} from "@/stores/unidade";
import {Processo, TipoProcesso, type Unidade} from "@/types/tipos";
import {logger} from "@/utils";
import {deveNotificarGlobalmente, ehErroAxios, extrairErrosGenericos, normalizarErro} from "@/utils/apiError";
import {
    aplicarSelecaoDiretaUnidadesComEquipePropria,
    filtrarSelecionadasPorElegibilidade,
    listarUnidadesComEquipePropriaSelecionadas,
    removerUnidadesSemEquipe
} from "@/views/processoCadastroUnidades";

type FormFieldsRef = InstanceType<typeof import("@/components/processo/ProcessoFormFields.vue").default> | null;

type ModalAcaoBlocoRef = {
    abrir: () => void;
    fechar: () => void;
    setProcessando: (valor: boolean) => void;
    setErro: (mensagem: string | null) => void;
};

interface TelaCadastroRefs {
    formFieldsRef: Ref<FormFieldsRef>;
    modalUnidadesComEquipePropriaRef: Ref<ModalAcaoBlocoRef | null>;
}

function criarFormularioCadastro() {
    const formulario = useProcessoForm();
    const formData = computed({
        get: () => ({
            descricao: formulario.descricao.value,
            tipo: formulario.tipo.value,
            unidadesSelecionadas: formulario.unidadesSelecionadas.value,
            dataLimite: formulario.dataLimite.value
        }),
        set: (value) => {
            formulario.descricao.value = value.descricao;
            formulario.tipo.value = value.tipo;
            formulario.unidadesSelecionadas.value = value.unidadesSelecionadas;
            formulario.dataLimite.value = value.dataLimite;
        }
    });

    return {formulario, formData};
}

function criarEstadoTela() {
    const isSaving = ref(false);
    const isStarting = ref(false);
    const isRemoving = ref(false);
    const unidades = ref<Unidade[]>([]);
    const isLoadingUnidades = ref(false);
    const mostrarModalConfirmacao = ref(false);
    const mostrarModalRemocao = ref(false);
    const processoEditando = ref<Processo | null>(null);
    const isLoadingData = ref(false);
    const carregamentoInicialConcluido = ref(false);
    const inicializando = ref(false);

    return {
        carregamentoInicialConcluido,
        inicializando,
        isLoadingData,
        isLoadingUnidades,
        isRemoving,
        isSaving,
        isStarting,
        mostrarModalConfirmacao,
        mostrarModalRemocao,
        processoEditando,
        unidades
    };
}

function criarTratadorErros({
    formulario,
    clear,
    focarPrimeiroErroInvalido,
    notify,
    notifyStructured
}: {
    formulario: ReturnType<typeof useProcessoForm>;
    clear: () => void;
    focarPrimeiroErroInvalido: () => Promise<void>;
    notify: (mensagem: string, variante?: VarianteAlerta, dispensavel?: boolean) => void;
    notifyStructured: (mensagem: string, detalhes: string[], opcoes?: { variante?: VarianteAlerta; stackTrace?: string }) => void;
}) {
    return function handleApiErrors(error: unknown, titulo: string, mensagemPadrao: string) {
        formulario.limparErros();
        clear();

        const erroNormalizado = normalizarErro(error);
        const usarErroEstruturado = ehErroAxios(error) || !!erroNormalizado.erros?.length;

        if (usarErroEstruturado) {
            formulario.aplicarErroNormalizado(erroNormalizado);
            const errosGenericos = extrairErrosGenericos(erroNormalizado);

            if (!formulario.temErros() || errosGenericos.length > 0) {
                notifyStructured(erroNormalizado.mensagem || mensagemPadrao, errosGenericos, {
                    variante: "danger",
                    stackTrace: erroNormalizado.stackTrace || undefined,
                });
                globalThis.scrollTo(0, 0);
            } else {
                void focarPrimeiroErroInvalido();
            }
        } else {
            notify(mensagemPadrao, "danger");
        }

        if (deveNotificarGlobalmente(erroNormalizado)) {
            logger.error(`${titulo}:`, error);
        }
    };
}

function criarFluxoCarga({
    formulario,
    formFieldsRef,
    route,
    router,
    notify,
    unidadeStore,
    unidades,
    isLoadingData,
    isLoadingUnidades,
    processoEditando,
    inicializando,
    carregamentoInicialConcluido
}: {
    formulario: ReturnType<typeof useProcessoForm>;
    formFieldsRef: Ref<FormFieldsRef>;
    route: ReturnType<typeof useRoute>;
    router: ReturnType<typeof useRouter>;
    notify: (mensagem: string, variante?: VarianteAlerta, dispensavel?: boolean) => void;
    unidadeStore: ReturnType<typeof useUnidadeStore>;
    unidades: Ref<Unidade[]>;
    isLoadingData: Ref<boolean>;
    isLoadingUnidades: Ref<boolean>;
    processoEditando: Ref<Processo | null>;
    inicializando: Ref<boolean>;
    carregamentoInicialConcluido: Ref<boolean>;
}) {
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
        try {
            const unidadesMapeadas = await unidadeStore.garantirArvoreElegibilidade(tipoProcesso, codigoProcesso);
            const unidadesSemSemEquipe = removerUnidadesSemEquipe(unidadesMapeadas);
            unidades.value = unidadesSemSemEquipe;
            sincronizarUnidadesSelecionadasElegiveis(unidadesSemSemEquipe);
        } catch (error) {
            if (isErroCanceladoHttp(error)) {
                return;
            }
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
            if (processo.situacao !== "CRIADO") {
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
            if (isErroCanceladoHttp(error)) {
                return;
            }
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

    return {buscarUnidadesParaProcesso};
}

function criarFluxoMutacoes({
    formulario,
    handleApiErrors,
    notify,
    modalUnidadesComEquipePropriaRef,
    router,
    toastStore,
    atualizarFluxoProcesso,
    mostrarModalConfirmacao,
    mostrarModalRemocao,
    processoEditando,
    isSaving,
    isStarting,
    isRemoving,
    unidadesComEquipePropriaSelecionadas,
    idsUnidadesComEquipePropriaSelecionadas
}: {
    formulario: ReturnType<typeof useProcessoForm>;
    handleApiErrors: (error: unknown, titulo: string, mensagemPadrao: string) => void;
    notify: (mensagem: string, variante?: VarianteAlerta, dispensavel?: boolean) => void;
    modalUnidadesComEquipePropriaRef: Ref<ModalAcaoBlocoRef | null>;
    router: ReturnType<typeof useRouter>;
    toastStore: ReturnType<typeof useToastStore>;
    atualizarFluxoProcesso: () => void;
    mostrarModalConfirmacao: Ref<boolean>;
    mostrarModalRemocao: Ref<boolean>;
    processoEditando: Ref<Processo | null>;
    isSaving: Ref<boolean>;
    isStarting: Ref<boolean>;
    isRemoving: Ref<boolean>;
    unidadesComEquipePropriaSelecionadas: Ref<Unidade[]>;
    idsUnidadesComEquipePropriaSelecionadas: Ref<number[]>;
}) {
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
            atualizarFluxoProcesso();
            await router.push("/painel");
            formulario.limpar();
        } catch (error) {
            handleApiErrors(error, "Erro ao salvar processo", "Não foi possível salvar o processo.");
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
                handleApiErrors(error, "Erro ao criar processo", TEXTOS.processo.cadastro.ERRO_CRIAR_PARA_INICIAR);
                return;
            }

            if (!formulario.tipo.value) {
                throw new Error("Tipo não definido");
            }
            await processoService.iniciarProcesso(codigoProcesso, formulario.tipo.value, codigosDiretos);

            toastStore.setPending(TEXTOS_SUCESSO_PROCESSO.PROCESSO_INICIADO);
            atualizarFluxoProcesso();
            await router.push("/painel");
            mostrarModalConfirmacao.value = false;
            modalUnidadesComEquipePropriaRef.value?.fechar();
        } catch (error) {
            mostrarModalConfirmacao.value = false;
            modalUnidadesComEquipePropriaRef.value?.fechar();
            handleApiErrors(error, "Erro ao iniciar processo", TEXTOS.processo.cadastro.ERRO_INICIAR_PROCESSO);
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
            atualizarFluxoProcesso();
            await router.push("/painel");
            formulario.limpar();
            mostrarModalRemocao.value = false;
        } catch (error) {
            mostrarModalRemocao.value = false;
            handleApiErrors(error, "Erro ao remover processo", TEXTOS.processo.cadastro.ERRO_REMOVER_PROCESSO);
        } finally {
            isRemoving.value = false;
        }
    }

    return {confirmarIniciarProcesso, confirmarRemocao, confirmarSelecaoUnidadesComEquipePropria, notify, salvarProcesso};
}

export function useProcessoCadastroTela({formFieldsRef, modalUnidadesComEquipePropriaRef}: TelaCadastroRefs) {
    const {formulario, formData} = criarFormularioCadastro();
    const tela = criarEstadoTela();
    const route = useRoute();
    const router = useRouter();
    const toastStore = useToastStore();
    const unidadeStore = useUnidadeStore();
    const {atualizarFluxoProcesso} = useInvalidacaoNavegacao();
    const {notificacao, notify, notifyStructured, clear} = useNotification();
    const {mostrarDiagnosticoOrganizacional} = usePerfil();
    const {focarPrimeiroErroInvalido} = useValidacaoFormulario();
    const anyLoading = computed(() => tela.isSaving.value || tela.isStarting.value || tela.isRemoving.value);
    const salvarDesabilitado = computed(() => formulario.isFormInvalid.value || tela.isLoadingData.value || anyLoading.value);
    const iniciarDesabilitado = computed(() => formulario.isFormInvalid.value || tela.isLoadingData.value || anyLoading.value);
    const unidadesComEquipePropriaSelecionadas = computed(() =>
        listarUnidadesComEquipePropriaSelecionadas(tela.unidades.value, formulario.unidadesSelecionadas.value)
    );
    const idsUnidadesComEquipePropriaSelecionadas = computed(() =>
        unidadesComEquipePropriaSelecionadas.value.map((unidade) => unidade.codigo)
    );
    const diagnostico = useDiagnosticoOrganizacionalAlert(tela.unidades, mostrarDiagnosticoOrganizacional);
    const handleApiErrors = criarTratadorErros({
        formulario,
        clear,
        focarPrimeiroErroInvalido,
        notify,
        notifyStructured
    });
    const {buscarUnidadesParaProcesso} = criarFluxoCarga({
        formulario,
        formFieldsRef,
        route,
        router,
        notify,
        unidadeStore,
        unidades: tela.unidades,
        isLoadingData: tela.isLoadingData,
        isLoadingUnidades: tela.isLoadingUnidades,
        processoEditando: tela.processoEditando,
        inicializando: tela.inicializando,
        carregamentoInicialConcluido: tela.carregamentoInicialConcluido
    });
    const {confirmarIniciarProcesso, confirmarRemocao, confirmarSelecaoUnidadesComEquipePropria, salvarProcesso} = criarFluxoMutacoes({
        formulario,
        handleApiErrors,
        notify,
        modalUnidadesComEquipePropriaRef,
        router,
        toastStore,
        atualizarFluxoProcesso,
        mostrarModalConfirmacao: tela.mostrarModalConfirmacao,
        mostrarModalRemocao: tela.mostrarModalRemocao,
        processoEditando: tela.processoEditando,
        isSaving: tela.isSaving,
        isStarting: tela.isStarting,
        isRemoving: tela.isRemoving,
        unidadesComEquipePropriaSelecionadas,
        idsUnidadesComEquipePropriaSelecionadas
    });

    return {
        anyLoading,
        buscarUnidadesParaProcesso,
        carregandoDiagnosticoOrganizacional: diagnostico.carregandoDiagnosticoOrganizacional,
        clear,
        confirmarIniciarProcesso,
        confirmarRemocao,
        confirmarSelecaoUnidadesComEquipePropria,
        dataLimite: formulario.dataLimite,
        descricao: formulario.descricao,
        dispensarAlertaDiagnostico: diagnostico.dispensarAlertaDiagnostico,
        exibirAlertaDiagnostico: diagnostico.exibirAlertaDiagnostico,
        fieldErrors: formulario.fieldErrors,
        formData,
        gruposDiagnostico: diagnostico.gruposDiagnostico,
        iniciarDesabilitado,
        isFormInvalid: formulario.isFormInvalid,
        isLoadingData: tela.isLoadingData,
        isLoadingUnidades: tela.isLoadingUnidades,
        isRemoving: tela.isRemoving,
        isSaving: tela.isSaving,
        isStarting: tela.isStarting,
        mostrarModalConfirmacao: tela.mostrarModalConfirmacao,
        mostrarModalRemocao: tela.mostrarModalRemocao,
        notificacao,
        notify,
        notifyStructured,
        processoEditando: tela.processoEditando,
        resumoDiagnostico: diagnostico.resumoDiagnostico,
        salvarDesabilitado,
        salvarProcesso,
        tipo: formulario.tipo,
        unidades: tela.unidades,
        unidadesComEquipePropriaSelecionadas,
        unidadesSelecionadas: formulario.unidadesSelecionadas,
        unidadesSemResponsavel: diagnostico.unidadesSemResponsavel,
    };
}
